#!/usr/bin/env python3

import argparse
import json
from pathlib import Path


def load_json(path: Path):
    with path.open("r", encoding="utf-8") as f:
        return json.load(f)


def format_delta(current: float, baseline: float) -> str:
    if baseline == 0:
        return "N/A"
    pct = ((current - baseline) / baseline) * 100.0
    return f"{pct:+.2f}%"


def evaluate_metric(name: str, rule: dict, current_value: float):
    baseline = float(rule["baseline"])

    if "max_absolute" in rule:
        max_abs = float(rule["max_absolute"])
        passed = current_value <= max_abs
        verdict = "PASS" if passed else "FAIL"
        delta = format_delta(current_value, baseline)
        return {
            "metric": name,
            "current": current_value,
            "baseline": baseline,
            "delta": delta,
            "verdict": verdict,
        }, passed

    max_regression_pct = float(rule["max_regression_pct"])
    allowed = baseline * (1.0 + max_regression_pct / 100.0)
    passed = current_value <= allowed
    verdict = "PASS" if passed else "FAIL"
    delta = format_delta(current_value, baseline)
    return {
        "metric": name,
        "current": current_value,
        "baseline": baseline,
        "delta": delta,
        "verdict": verdict,
    }, passed


def render_markdown(rows):
    lines = [
        "| Metric | Current | Baseline | Delta | Verdict |",
        "|---|---:|---:|---:|---|",
    ]
    for row in rows:
        lines.append(
            f"| {row['metric']} | {row['current']:.2f} | {row['baseline']:.2f} | {row['delta']} | {row['verdict']} |"
        )
    return "\n".join(lines) + "\n"


def main():
    parser = argparse.ArgumentParser(description="Performance regression gate")
    parser.add_argument("--baseline", required=True, help="Baseline JSON path")
    parser.add_argument("--current", required=True, help="Current metrics JSON path")
    parser.add_argument("--report", required=True, help="Markdown report output path")
    args = parser.parse_args()

    baseline = load_json(Path(args.baseline))
    current = load_json(Path(args.current))

    rules = baseline.get("metrics", {})
    missing = [name for name in rules.keys() if name not in current]
    if missing:
        raise SystemExit(f"Missing metrics in current file: {', '.join(missing)}")

    rows = []
    all_passed = True

    for metric_name, rule in rules.items():
        row, passed = evaluate_metric(metric_name, rule, float(current[metric_name]))
        rows.append(row)
        all_passed = all_passed and passed

    report = render_markdown(rows)
    report_path = Path(args.report)
    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(report, encoding="utf-8")

    print(report)
    if not all_passed:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
