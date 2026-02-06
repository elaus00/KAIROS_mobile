#!/usr/bin/env python3

import argparse
import json
from pathlib import Path
from typing import Dict, Iterable, Tuple


MetricMapping = Dict[str, Iterable[Tuple[str, str]]]

MAPPING: MetricMapping = {
    "cold_start_ms": (("coldStart", "timeToInitialDisplayMs"),),
    "first_input_latency_ms": (("firstInputLatency", "first_input_latencySumMs"),),
    "capture_save_completion_ms": (("captureSaveCompletionTime", "capture_save_completionSumMs"),),
    "ai_classification_completion_ms": (
        ("aiClassificationCompletionTime", "ai_classification_completionSumMs"),
    ),
    # Prefer search scenario; fallback to notes scenario when search is unavailable.
    "search_scroll_jank_percent": (
        ("searchScrollJank", "gfxFrameJankPercent"),
        ("notesScrollJank", "gfxFrameJankPercent"),
    ),
}


def load_json(path: Path):
    with path.open("r", encoding="utf-8") as f:
        return json.load(f)


def find_benchmark(benchmarks, name: str):
    for benchmark in benchmarks:
        if benchmark.get("name") == name:
            return benchmark
    return None


def read_metric_median(benchmark: dict, metric_name: str) -> float:
    metric = benchmark.get("metrics", {}).get(metric_name)
    if metric is None:
        raise KeyError(metric_name)
    if isinstance(metric, (int, float)):
        return float(metric)
    if isinstance(metric, dict):
        if "median" in metric:
            return float(metric["median"])
        if "minimum" in metric and "maximum" in metric and metric["minimum"] == metric["maximum"]:
            return float(metric["minimum"])
    raise ValueError(f"Unsupported metric format for '{metric_name}' in benchmark '{benchmark.get('name')}'")


def extract_metrics(data: dict) -> dict:
    benchmarks = data.get("benchmarks", [])
    output = {}
    missing = []

    for output_key, candidates in MAPPING.items():
        extracted = None
        for benchmark_name, metric_name in candidates:
            benchmark = find_benchmark(benchmarks, benchmark_name)
            if benchmark is None:
                continue
            try:
                extracted = read_metric_median(benchmark, metric_name)
                break
            except KeyError:
                continue

        if extracted is None:
            missing.append(output_key)
        else:
            output[output_key] = extracted

    if missing:
        raise SystemExit(
            "Failed to extract required metrics: " + ", ".join(missing)
        )
    return output


def main():
    parser = argparse.ArgumentParser(description="Extract normalized current metrics from benchmarkData.json")
    parser.add_argument("--input", required=True, help="Macrobenchmark benchmarkData.json path")
    parser.add_argument("--output", required=True, help="Output current_metrics.json path")
    args = parser.parse_args()

    data = load_json(Path(args.input))
    current_metrics = extract_metrics(data)

    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(current_metrics, indent=2) + "\n", encoding="utf-8")

    print(json.dumps(current_metrics, indent=2))


if __name__ == "__main__":
    main()
