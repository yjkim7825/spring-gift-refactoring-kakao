"""Report wording and symbols banned in docs/ (see CLAUDE.md, section 6).

Usage:
    py -I tools/check_doc_style.py docs/*.md    # exit 1 if anything is found

Fenced code blocks are skipped.
"""
import re
import sys

RULES = [
    ("middle dot", re.compile("[·‧・]")),
    ("em/en dash", re.compile("[–—]")),
    ("arrow", re.compile("[←-⇿⟰-⟿]")),
    ("bold emphasis", re.compile(r"\*\*")),
    ("cliche", re.compile("다양한|다양하게|한눈에|사실상|자연스럽게|원활|효율적|손쉽게|매끄럽|무엇보다|요컨대")),
]


def main(paths):
    found = 0
    for path in paths:
        in_code = False
        with open(path, encoding="utf-8") as f:
            for no, line in enumerate(f, 1):
                if line.strip().startswith("```"):
                    in_code = not in_code
                    continue
                if in_code:
                    continue
                for name, pattern in RULES:
                    for m in pattern.finditer(line):
                        found += 1
                        print(f"{path}:{no}: {name}: {m.group(0)!r}: {line.strip()[:80]}")
    return 1 if found else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
