# External Sort (Tri Externe) – Database Systems TP2

Java implementation of the **external sorting algorithm** used when datasets are **too large to fit into RAM**, relying on disk fragments and multi-way merging.

---

## Algorithm Overview

The algorithm works in two phases:

1. **Run generation**
   - Read CSV blocks of size `M`.
   - Sort each block in memory.
   - Write sorted blocks as temporary fragments.

2. **Multi-way merge**
   - Merge up to `M-1` fragments at once.
   - Continue until a single sorted file remains.

---

## Project Structure

- `src/tri_externe/`
  - `TriExterne.java` — External sort implementation.
  - `Comparateur.java` — Comparator for tuple ordering.
- `data/` — Input CSV files.
- `tmp/` — Temporary fragments generated during execution.

---

## Usage

### Compilation
```bash
javac src/tri_externe/*.java
```

### Execution
```bash
java -cp src tri_externe.TriExterne data/communes.csv col1;col2;...
```

Example:
```bash
java -cp src tri_externe.TriExterne data/communes.csv REG;COM
```

Sorts the dataset by region and then by commune name.

---

## Features

- Multi-column CSV sorting.
- Handles datasets larger than memory.
- Fragment generation and multi-way merging.
- Extendable comparator logic.

---

## Classes and Methods

### TriExterne
- `TriExterne(String path, String colonnes, int M)`
- `trier()`
- `creerFragmentsInitiaux()`
- `fusion(int niveau, int nombre)`
- `testComparateur()`

### Comparateur
- `Comparateur(int[] indices)`
- `compare(String[] t1, String[] t2)`

---

## Educational Context – What I Learned

This project was developed in the context of the **Advanced Databases** course (Université Clermont Auvergne, 2025).  
It helped me to better understand several database system concepts:

- How a DBMS performs sorting operations (`ORDER BY`, `GROUP BY`, `DISTINCT`) using external sort.
- The importance of memory constraints and how to design scalable algorithms.
- The two-phase sort-merge algorithm.
- Multi-column comparator design similar to SQL ordering.
- The role of temporary files and disk I/O in query processing.

Overall, this assignment bridged theory and practice by turning course concepts into a working Java program handling real data.

---

## Possible Extensions

- Numeric vs textual column typing.
- Heap-based merge optimization.