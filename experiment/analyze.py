"""Summarise measured CSVs and render publication-quality experiment figures.

Python >=3.10; matplotlib is the only third-party dependency.
Use --summary-only to generate the summary with the Python standard library.
"""
from __future__ import annotations

import argparse
import collections
import csv
import hashlib
import json
import math
from pathlib import Path
import statistics
import sys

ROOT = Path(__file__).resolve().parent
RESULTS = ROOT / "results"
FIGURES = ROOT / "figures"
SOURCE = ROOT.parent / "src/ch/zhaw/ads/RangZuerich.csv"
NAVY, TEAL, AMBER, BG = "#14243B", "#087F8C", "#E9A23B", "#F7F9FC"
GREY, GRID = "#66758A", "#DCE3EB"


def read_data(name):
    with (RESULTS / name).open(encoding="utf-8", newline="") as handle:
        return list(csv.DictReader(handle))


def quantile(values, p):
    """Linearly interpolated sample quantile (Hyndman/Fan type 7)."""
    ordered = sorted(values)
    position = (len(ordered) - 1) * p
    lo, hi = math.floor(position), math.ceil(position)
    return ordered[lo] + (ordered[hi] - ordered[lo]) * (position - lo)


def describe(values, monte_carlo=False):
    result = {
        "count": len(values), "min": min(values), "max": max(values),
        "mean": statistics.mean(values), "median": statistics.median(values),
        "population_stddev": statistics.pstdev(values),
        "quantiles": {str(p): quantile(values, p) for p in (.025, .25, .5, .75, .975)},
    }
    if monte_carlo:
        se = statistics.stdev(values) / math.sqrt(len(values))
        result["mean_standard_error"] = se
        result["mean_95pct_normal_ci"] = [result["mean"] - 1.96 * se,
                                            result["mean"] + 1.96 * se]
    return result


def summarize():
    binary = SOURCE.read_bytes()
    source_rows = list(csv.reader(binary.decode("utf-8-sig").splitlines(), delimiter=";"))
    source_rows = [row for row in source_rows if row]
    times = collections.Counter(row[1] for row in source_rows)
    row_counts = collections.Counter(tuple(row) for row in source_rows)
    n = len(source_rows)
    log_factorial = math.lgamma(n + 1) / math.log(10)
    random_rows = read_data("full_random.csv")
    exact_rows = read_data("exact8.csv")
    selected = read_data("selected8.csv")
    references = {row["order"]: {k: int(row[k]) for k in ("height", "comparisons")}
                  for row in read_data("references.csv")}
    metrics = lambda rows, mc: {k: describe([int(row[k]) for row in rows], mc)
                               for k in ("height", "comparisons")}
    exact_metrics = metrics(exact_rows, False)
    mean_formula = 2 * (8 + 1) * sum(1 / i for i in range(1, 9)) - 4 * 8
    assert len(exact_rows) == math.factorial(8)
    assert exact_metrics["height"]["min"] == 4
    assert exact_metrics["comparisons"]["min"] == 13
    assert exact_metrics["comparisons"]["max"] == 28
    assert math.isclose(exact_metrics["comparisons"]["mean"], mean_formula, rel_tol=1e-12)
    assert references["descending"]["height"] == n
    assert references["descending"]["comparisons"] == n * (n - 1) // 2
    summary = {
        "source": {
            "path": "src/ch/zhaw/ads/RangZuerich.csv", "encoding": "UTF-8",
            "sha256": hashlib.sha256(binary).hexdigest(), "n": n,
            "distinct_times": len(times), "duplicate_time_entries_beyond_first": n - len(times),
            "maximum_time_multiplicity": max(times.values()),
            "distinct_csv_rows": len(row_counts), "minimum_time": min(times),
            "maximum_time": max(times),
            "log10_labelled_row_permutations": log_factorial,
            "log10_distinct_csv_row_sequences": log_factorial - sum(
                math.lgamma(c + 1) / math.log(10) for c in row_counts.values()),
            "log10_distinct_key_sequences": log_factorial - sum(
                math.lgamma(c + 1) / math.log(10) for c in times.values()),
        },
        "method": {
            "seed": 20261019, "rng": "java.util.Random", "shuffle": "Fisher-Yates",
            "insertion_rule": "key <= current -> left; key > current -> right",
            "height_definition": "Number of levels; empty=0, leaf=1",
            "cost_definition": "One key comparison per visited existing node while inserting; sum of insertion depths",
            "traversal_visits_per_full_run": n,
            "timing": "No wall-clock times were measured",
            "quantile_method": "Linear interpolation, Hyndman/Fan type 7",
            "ci_method": "Monte-Carlo mean +/-1.96 * sample SD / sqrt(trials); normal approximation",
        },
        "full_random": metrics(random_rows, True),
        "references": references,
        "unrestricted_binary_tree_height_lower_bound": math.ceil(math.log2(n + 1)),
        "median_reference_is_proven_global_optimum": False,
        "exact8": {
            "selected_times": [row["time"] for row in selected],
            "selection": "8 equally spaced indices in sorted distinct CSV times, including extremes",
            "metrics": exact_metrics, "theoretical_mean_comparisons": mean_formula,
            "height_frequencies": dict(sorted(collections.Counter(int(row["height"]) for row in exact_rows).items())),
            "comparison_frequencies": dict(sorted(collections.Counter(int(row["comparisons"]) for row in exact_rows).items())),
        },
        "scaling": {
            n_value: metrics([row for row in read_data("scaling.csv") if int(row["n"]) == n_value], True)
            for n_value in (64, 128, 256, 512, 1024, 2048, n)
        },
    }
    (RESULTS / "summary.json").write_text(json.dumps(summary, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    return summary


def swiss(value, digits=0):
    return f"{value:,.{digits}f}".replace(",", "'")


def figures(summary):
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    import matplotlib.ticker as ticker
    FIGURES.mkdir(exist_ok=True)
    plt.rcParams.update({
        "font.family": "DejaVu Sans", "font.size": 11,
        "text.color": NAVY, "axes.labelcolor": NAVY, "axes.edgecolor": GRID,
        "xtick.color": GREY, "ytick.color": GREY,
        "figure.facecolor": BG, "axes.facecolor": BG,
        "axes.spines.top": False, "axes.spines.right": False,
        "axes.spines.left": False, "axes.spines.bottom": False,
        "grid.color": GRID, "grid.linewidth": .7,
        "axes.titleweight": "bold", "axes.titlepad": 16,
        "pdf.fonttype": 42, "ps.fonttype": 42,
        "savefig.facecolor": BG,
    })

    def save(fig, stem):
        fig.savefig(FIGURES / f"{stem}.pdf", bbox_inches="tight", pad_inches=.15)
        fig.savefig(FIGURES / f"{stem}.png", dpi=190, bbox_inches="tight", pad_inches=.15)
        plt.close(fig)

    def clean(ax, direction="y"):
        ax.grid(axis=direction)
        ax.set_axisbelow(True)
        ax.tick_params(length=0, pad=6)

    # Slide-ready main chart 1: exhaustive experiment, best/average/worst are exact.
    fig, axes = plt.subplots(1, 2, figsize=(12.5, 4.4), gridspec_kw={"width_ratios": [1, 1.6]})
    hfreq = summary["exact8"]["height_frequencies"]
    cfreq = summary["exact8"]["comparison_frequencies"]
    total = math.factorial(8)
    for ax, freq, label in zip(axes, (hfreq, cfreq), ("Baumhöhe (Ebenen)", "Vergleiche beim Einfügen")):
        keys = list(freq)
        percentages = [100 * freq[key] / total for key in keys]
        colors = [TEAL if key == min(keys) else AMBER if key == max(keys) else NAVY for key in keys]
        ax.bar(keys, percentages, width=.76, color=colors, zorder=3)
        ax.set_xlabel(label, labelpad=10)
        ax.set_ylabel("Anteil aller Reihenfolgen (%)")
        ax.yaxis.set_major_formatter(ticker.PercentFormatter(xmax=100, decimals=0))
        ax.xaxis.set_major_locator(ticker.MaxNLocator(integer=True))
        clean(ax)
    axes[0].set_title("Höhe: 4 bis 8 Ebenen", loc="left")
    axes[0].set_ylim(0, 62)
    for h, count in hfreq.items():
        axes[0].text(h, count / total * 100 + 1.1, f"{count / total * 100:.1f}%", ha="center", fontsize=10)
    mean = summary["exact8"]["metrics"]["comparisons"]["mean"]
    axes[1].set_title("Aufwand: Best, Average und Worst", loc="left")
    axes[1].axvline(mean, color=TEAL, linewidth=2, linestyle="--", zorder=4)
    axes[1].set_xticks(range(13, 29, 2))
    axes[1].set_ylim(0, 28)
    axes[1].text(mean + .25, 25.4, f"Mittelwert {mean:.2f}", color=TEAL, fontsize=11)
    axes[1].text(13, 22, "Best\n13", ha="center", color=TEAL, weight="bold", fontsize=11)
    axes[1].text(28, 22, "Worst\n28", ha="center", color="#A9680F", weight="bold", fontsize=11)
    fig.subplots_adjust(wspace=.32, bottom=.21)
    fig.text(.5, .015, "8 verschiedene Zeiten · alle 8! = 40'320 Reihenfolgen · jeder Fall genau einmal", ha="center", fontsize=11, color=GREY)
    save(fig, "exact8_distribution")

    random = summary["full_random"]
    rows = read_data("full_random.csv")
    heights = [int(row["height"]) for row in rows]
    mean_h = random["height"]["mean"]
    q_lo, q_hi = (random["height"]["quantiles"][p] for p in ("0.025", "0.975"))
    ref = summary["references"]

    def height_hist(ax, compact=False):
        ax.axvspan(q_lo - .5, q_hi + .5, color=TEAL, alpha=.07, zorder=0)
        bins = [h - .5 for h in range(min(heights), max(heights) + 2)]
        ax.hist(heights, bins=bins, weights=[100 / len(heights)] * len(heights),
                color=TEAL, rwidth=.83, zorder=3)
        ax.axvline(mean_h, color=NAVY, linestyle="--", linewidth=1.8, zorder=4)
        ax.set_xlabel("Baumhöhe (Ebenen)", labelpad=10)
        ax.set_ylabel("Anteil der Stichproben (%)")
        ax.yaxis.set_major_formatter(ticker.PercentFormatter(xmax=100, decimals=0))
        ax.xaxis.set_major_locator(ticker.MaxNLocator(integer=True))
        ymax = ax.get_ylim()[1]
        ax.set_ylim(0, ymax * 1.25)
        ax.text(.97, .94, f"Mittelwert {mean_h:.2f}\n2.5.–97.5. Perzentil: {q_lo:.0f}–{q_hi:.0f}",
                ha="right", va="top", transform=ax.transAxes, fontsize=10.5,
                bbox={"facecolor": BG, "edgecolor": "none", "alpha": .92, "pad": 3})
        clean(ax)

    def comparison_bars(ax):
        labels = ["Median zuerst*", "Zufällig (Mittel)", "CSV-Original", "Aufsteigend", "Absteigend"]
        values = [ref["median_first"]["comparisons"], random["comparisons"]["mean"],
                  ref["original"]["comparisons"], ref["ascending"]["comparisons"], ref["descending"]["comparisons"]]
        colors = [TEAL, NAVY, GREY, AMBER, "#B46D14"]
        bars = ax.barh(labels, values, color=colors, height=.58, zorder=3)
        ax.set_xscale("log")
        ax.set_xlim(10_000, 18_000_000)
        ax.set_xticks([10_000, 100_000, 1_000_000, 10_000_000], ["10 Tsd.", "100 Tsd.", "1 Mio.", "10 Mio."])
        ax.xaxis.set_minor_locator(ticker.NullLocator())
        ax.invert_yaxis()
        ax.set_xlabel("Einfügevergleiche · logarithmische Achse", labelpad=10)
        for bar, value in zip(bars, values):
            ax.text(value * 1.15, bar.get_y() + bar.get_height() / 2, swiss(value),
                    va="center", fontsize=10, color=NAVY)
        clean(ax, "x")

    # Main chart 2 combines the random distribution with clearly separated controls.
    fig, axes = plt.subplots(1, 2, figsize=(13, 4.5), gridspec_kw={"width_ratios": [1.1, 1]})
    height_hist(axes[0], True)
    comparison_bars(axes[1])
    axes[0].set_title("10'000 zufällige Reihenfolgen", loc="left")
    axes[1].set_title("Gleiche Daten, anderer Aufwand", loc="left")
    fig.subplots_adjust(wspace=.42, bottom=.25)
    fig.text(.5, .065, "3'166 Teilnehmer · immer 3'166 Inorder-Besuche · Seed 20261019", ha="center", fontsize=11, color=NAVY)
    fig.text(.5, .01, "* Median zuerst = flache Referenz, kein bewiesenes globales Optimum. Gemessen: Vergleiche, keine Millisekunden.", ha="center", fontsize=9.5, color=GREY)
    save(fig, "full_csv_main")

    # Standalone variants for extra slides or a written appendix.
    fig, ax = plt.subplots(figsize=(10, 5.2))
    height_hist(ax)
    ax.set_title("Baumhöhen bei 10'000 zufälligen CSV-Reihenfolgen", loc="left")
    fig.subplots_adjust(bottom=.26)
    fig.text(.5, .08, "Separat konstruierte Referenzen: Median zuerst h=14 · Original h=42 · Absteigend h=3'166", ha="center", fontsize=10, color=GREY)
    ci = random["height"]["mean_95pct_normal_ci"]
    fig.text(.5, .025, f"95%-Konfidenzintervall des Mittelwerts: [{ci[0]:.3f}; {ci[1]:.3f}] · dies ist nicht das Perzentilintervall", ha="center", fontsize=9.5, color=GREY)
    save(fig, "full_height_distribution")

    fig, ax = plt.subplots(figsize=(10.5, 4.8))
    comparison_bars(ax)
    ax.set_title("Einfügeaufwand für dieselben 3'166 CSV-Zeilen", loc="left")
    fig.subplots_adjust(left=.2, bottom=.23)
    fig.text(.5, .03, "* Flache Referenz; Duplikate werden links eingefügt. Dazu kommen stets 3'166 Inorder-Besuche.", ha="center", fontsize=10, color=GREY)
    save(fig, "full_comparison_cases")

    fig, ax = plt.subplots(figsize=(10, 5))
    scaling = summary["scaling"]
    sizes = sorted(scaling)
    means = [scaling[n]["comparisons"]["mean"] for n in sizes]
    ax.plot(sizes, means, marker="o", color=TEAL, linewidth=2.5, label="100 Zufallsfolgen je n: Mittelwert")
    ax.plot(sizes, [2 * (n + 1) * sum(1 / k for k in range(1, n + 1)) - 4 * n for n in sizes],
            color=NAVY, linestyle="--", label="Theorie für verschiedene Schlüssel: 2(n+1)Hₙ − 4n")
    ax.plot(sizes, [n * (n - 1) / 2 for n in sizes], color=AMBER, linewidth=2, label="Absteigend: n(n−1)/2")
    ax.set_xscale("log", base=2)
    ax.set_yscale("log")
    ax.set_xticks(sizes, [swiss(n) for n in sizes])
    ax.set_xlabel("Anzahl n · jeweils die ersten n CSV-Zeilen, danach gemischt")
    ax.set_ylabel("Einfügevergleiche")
    ax.set_title("Wachstum: zufällige und absteigende Reihenfolgen", loc="left")
    clean(ax, "both")
    ax.legend(loc="upper left", frameon=False, fontsize=9.5)
    fig.subplots_adjust(bottom=.19)
    save(fig, "scaling")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--summary-only", action="store_true")
    args = parser.parse_args()
    summary = summarize()
    if not args.summary_only:
        figures(summary)
    print(json.dumps({"references": summary["references"], "full_random": summary["full_random"],
                      "exact8": summary["exact8"]["metrics"]}, indent=2, ensure_ascii=False))
