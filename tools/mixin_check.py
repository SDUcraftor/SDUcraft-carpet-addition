#!/usr/bin/env python3
"""静态校验 preprocess 项目的 mixin 目标在指定 MC 版本上是否存在。

用法（在 IDE 终端）:
    python3 tools/mixin_check.py <mcVersion> <minecraft-merged.jar> [mixin-src-dir]

它会按 preprocess 的 #if 条件把源码还原成"目标 MC 版本的视图"，
再对每个 @Mixin / method = "..." / @At(target = "...") 用 javap 校验。
"""
import functools
import pathlib
import re
import subprocess
import sys

MC = int(sys.argv[1]) if len(sys.argv) > 1 else 260200
JAR = sys.argv[2]


def find_mixin_root(explicit=None):
    """定位 mixin 源码根。

    优先级：显式参数 > 项目里的 *.mixins.json 的 package 字段 > src/main/java 下的 mixin 目录。
    这样脚本不再假设某个具体项目的包名。
    """
    import json
    if explicit:
        return pathlib.Path(explicit)
    # 1) 从 mixins.json 的 package 推导
    for cfg in sorted(pathlib.Path('.').rglob('*.mixins.json')):
        if 'build' in cfg.parts or 'preprocessed' in cfg.parts:
            continue
        try:
            pkg = json.loads(cfg.read_text(encoding='utf-8')).get('package', '')
        except Exception:
            continue
        if not pkg:
            continue
        cand = cfg.parent.parent / pathlib.Path(pkg.replace('.', '/'))
        if cand.is_dir():
            return cand
        # processResources 展开后的资源目录：回退到 src/main/java 下同名包
        alt = pathlib.Path('src/main/java') / pathlib.Path(pkg.replace('.', '/'))
        if alt.is_dir():
            return alt
    # 2) 回退：src/main/java 下第一个名为 mixin 的目录
    base = pathlib.Path('src/main/java')
    if base.is_dir():
        for d in sorted(base.rglob('mixin')):
            if d.is_dir():
                return d
    return base


ROOT = find_mixin_root(sys.argv[3] if len(sys.argv) > 3 else None)

IMPORT_RE = re.compile(r'^import\s+(?:static\s+)?([\w.$]+);')
SIMPLE_RE = re.compile(r'^import\s+(?:static\s+)?[\w.$]*?(\w+)$')


def active_lines(path: pathlib.Path):
    """按 preprocess 条件返回目标版本的活跃行（去掉 //$$ 前缀）。"""
    out = []
    stack = []  # list of (kind, active)
    for lineno, raw in enumerate(path.read_text(encoding='utf-8').split('\n'), 1):
        s = raw.strip()
        m = re.match(r'^//#if\s+(.*)$', s)
        if m:
            stack.append(eval_cond(m.group(1)))
            continue
        m = re.match(r'^//#elseif\s+(.*)$', s)
        if m:
            if stack:
                parent_active = all(x for x in stack[:-1])
                stack[-1] = parent_active and eval_cond(m.group(1))
            continue
        if s.startswith('//#else'):
            if stack:
                parent_active = all(x for x in stack[:-1])
                stack[-1] = parent_active and not stack[-1]
            continue
        if s.startswith('//#endif'):
            if stack:
                stack.pop()
            continue
        if stack and not all(stack):
            continue
        if s.startswith('//$$'):
            s = s[4:].strip()
        out.append((lineno, s))
    return out


def eval_cond(cond: str) -> bool:
    expr = cond.replace('MC', str(MC))
    try:
        return bool(eval(expr, {'__builtins__': {}}, {}))
    except Exception:
        return True


@functools.lru_cache(maxsize=None)
def javap(cls: str):
    for candidate in (cls, inner_class_name(cls)):
        if candidate is None:
            continue
        r = subprocess.run(['javap', '-p', '-classpath', JAR, candidate],
                           capture_output=True, text=True)
        if r.returncode == 0:
            return r.stdout
    return None


def inner_class_name(cls: str):
    """a.b.C.D -> a.b.C$D (mixin 源码里常这么写内部类)"""
    if '.' not in cls:
        return None
    head, tail = cls.rsplit('.', 1)
    if not tail or not tail[0].isupper():
        return None
    if '$' in cls:
        return None
    return head + '$' + tail


@functools.lru_cache(maxsize=None)
def methods_of(cls: str):
    """返回 (方法名 -> [参数个数...], 父类 FQN 或 None)"""
    out = javap(cls)
    if out is None:
        return None, None
    names = {}
    for line in out.split('\n'):
        line = line.rstrip()
        if not line.endswith(';') or '(' not in line or line.startswith('Compiled'):
            continue
        m = re.match(r'^\s+[\w$.<>\[\],\s]+?([\w$<>]+)\((.*)\);\s*$', line)
        if not m:
            continue
        args = m.group(2).strip()
        depth = 0
        n = 0
        for ch in args:
            if ch == '<':
                depth += 1
            elif ch == '>':
                depth -= 1
            elif ch == ',' and depth == 0:
                n += 1
        if args:
            n += 1
        names.setdefault(m.group(1), []).append(n)
    sup = None
    m = re.search(r'\bextends\s+([\w.$]+)', out)
    if m:
        sup = m.group(1)
    return names, sup


def has_method(cls: str, name: str, argc=None, depth=0):
    if cls is None or depth > 10:
        return False
    names, sup = methods_of(cls)
    if names is None:
        return None  # 类不存在
    if name in names:
        if argc is None or argc in names[name]:
            return True
    return has_method(sup, name, argc, depth + 1)


def parse_desc_args(desc: str):
    """从 JVM 描述符参数部分粗略数参数个数"""
    depth = 0
    n = 0
    i = 0
    while i < len(desc):
        c = desc[i]
        if c == '[':
            i += 1
            continue
        if c == 'L':
            depth += 1
            j = desc.find(';', i)
            i = j + 1
            n += 1
            continue
        if c in 'BCDFIJSZ':
            n += 1
        i += 1
    return n


problems = []
for path in sorted(ROOT.rglob('*.java')):
    lines = active_lines(path)
    text = '\n'.join(l for _, l in lines)
    imports = {}
    for _, l in lines:
        m = IMPORT_RE.match(l)
        if m:
            fqn = m.group(1)
            imports[fqn.split('.')[-1]] = fqn

    def resolve(name: str):
        parts = name.split('.')
        if parts[0] in imports:
            base = imports[parts[0]]
            return base + ('.' + '.'.join(parts[1:]) if len(parts) > 1 else '')
        if '.' in name:
            return name
        return imports.get(name, 'net.minecraft.' + name)

    # @Mixin 目标
    targets = []
    for _, l in lines:
        m = re.search(r'@Mixin\(\s*(?:value\s*=\s*)?([\w.$]+)\.class', l)
        if m:
            targets.append(resolve(m.group(1)))
        m = re.search(r'@Mixin\(\s*targets\s*=\s*"([^"]+)"', l)
        if m:
            targets.append(m.group(1))
    if not targets:
        continue

    # method = "..." （可能跨行）
    for _, l in lines:
        for m in re.finditer(r'method\s*=\s*(\{[^}]*\}|"[^"]*")', l):
            raw = m.group(1)
            items = re.findall(r'"([^"]*)"', raw)
            for item in items:
                name = item.split('(')[0]
                if not name or name.startswith('lambda$') or name in ('<init>', '<clinit>'):
                    continue
                argc = None
                if '(' in item:
                    desc = item[item.index('(') + 1:]
                    argc = parse_desc_args(desc)
                ok = False
                for t in targets:
                    r = has_method(t, name, argc)
                    if r:
                        ok = True
                        break
                    if r is None:
                        problems.append((str(path), '  MIXIN TARGET CLASS MISSING: ' + t))
                        ok = True
                        break
                if not ok:
                    problems.append((str(path), f'  method "{item}" not found in {targets}'))

    # @At(target = "Lpkg/Cls;name(desc)ret")
    for _, l in lines:
        for m in re.finditer(r'target\s*=\s*"(L([\w/$]+);([\w$<>]+)(\([^"]*\)[^"]*)?)"', l):
            cls = m.group(2).replace('/', '.')
            name = m.group(3)
            desc = m.group(4) or ''
            argc = parse_desc_args(desc[1:desc.index(')')]) if desc.startswith('(') else None
            r = has_method(cls, name, argc)
            if r is False:
                problems.append((str(path), f'  @At target "{name}" not found in {cls}'))
            elif r is None:
                problems.append((str(path), f'  @At target CLASS MISSING: {cls}'))

if problems:
    print(f"发现 {len(problems)} 处问题（MC={MC}）:")
    for f, p in problems:
        print(f"{f}\n{p}")
else:
    print(f"未发现问题（MC={MC}）")
