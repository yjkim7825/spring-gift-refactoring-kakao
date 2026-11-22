"""Align Markdown table pipes in the raw source, counting East Asian wide characters as 2 columns.

Usage:
    py -I tools/align_md_tables.py docs/*.md            # rewrite files
    py -I tools/align_md_tables.py --check docs/*.md    # report only, exit 1 if any file would change

Ambiguous-width characters (for example x-sign) count as 1 column.
"""
import re
import sys
import unicodedata

SEP = re.compile(r"^:?-+:?$")


def width(s):
    w = 0
    for ch in s:
        if unicodedata.combining(ch) or ch in "‍️":
            continue
        w += 2 if unicodedata.east_asian_width(ch) in ("W", "F") else 1
    return w


def split_row(line):
    body = line.strip()
    if not (body.startswith("|") and body.endswith("|")):
        raise ValueError(f"table row must start and end with '|': {line}")
    return [c.strip() for c in body[1:-1].split("|")]


def format_table(lines):
    indent = re.match(r"^\s*", lines[0]).group(0)
    rows = [split_row(l) for l in lines]
    ncol = len(rows[0])
    if any(len(r) != ncol for r in rows):
        raise ValueError("rows have different column counts:\n" + "\n".join(lines))
    if len(rows) < 2 or not all(SEP.match(c) for c in rows[1]):
        raise ValueError("second row must be the separator row:\n" + "\n".join(lines))
    widths = [max(3, max(width(r[i]) for j, r in enumerate(rows) if j != 1)) for i in range(ncol)]
    out = []
    for j, r in enumerate(rows):
        if j == 1:
            cells = []
            for i, c in enumerate(r):
                left, right = c.startswith(":"), c.endswith(":") and len(c) > 1
                cells.append((":" if left else "") + "-" * (widths[i] - left - right) + (":" if right else ""))
        else:
            cells = [c + " " * (widths[i] - width(c)) for i, c in enumerate(r)]
        out.append(indent + "| " + " | ".join(cells) + " |")
    return out


def align(text):
    lines = text.split("\n")
    result, i, in_code = [], 0, False
    while i < len(lines):
        line = lines[i]
        if line.strip().startswith("```"):
            in_code = not in_code
        if not in_code and line.strip().startswith("|"):
            block = []
            while i < len(lines) and lines[i].strip().startswith("|"):
                block.append(lines[i])
                i += 1
            result.extend(format_table(block))
            continue
        result.append(line)
        i += 1
    return "\n".join(result)


def main(argv):
    check = "--check" in argv
    paths = [a for a in argv if a != "--check"]
    changed = []
    for path in paths:
        with open(path, encoding="utf-8") as f:
            original = f.read()
        aligned = align(original)
        if aligned != original:
            changed.append(path)
            if not check:
                with open(path, "w", encoding="utf-8", newline="") as f:
                    f.write(aligned)
    label = "needs alignment" if check else "aligned"
    for path in changed:
        print(f"{label}: {path}")
    return 1 if check and changed else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
