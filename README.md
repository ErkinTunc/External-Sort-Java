# External Sort (Tri Externe) – Database Systems TP2

This repository contains a Java implementation of the **external sorting algorithm** (“Tri Externe”), developed as part of the **Advanced Databases (Base de données avancée)** course (TP2).

External sorting is required when the dataset is **too large to fit into main memory (RAM)**. The algorithm is memory-constrained and uses disk-based fragments.

---

## 📖 Algorithm Overview

The algorithm proceeds in **two main phases**:

External sort is used when the dataset is **too large to fit into memory (RAM)**.  
The algorithm works in two main phases:

1. **Run generation:** The input file is read in blocks of size `M` (the memory cache). Each block is sorted and written to disk as a temporary fragment.
2. **Multi-way merge:** The sorted fragments are merged together (using up to `M-1` fragments simultaneously) until a single sorted output file remains.

---

## 📂 Project Structure

- `TriExterne.java` → Main class, manages external sort:
  - Generates initial fragments.
  - Performs multi-way merge.
  - Handles CSV reading/writing.
- `Comparateur.java` → Comparator that sorts tuples (rows) according to one or multiple columns.

---

## 🛠️ Usage

### Compilation

```bash
javac *.java
```

### Execution

```bash
java TriExterne input.csv col1;col2;...
```

- `input.csv` → CSV file with a **header row** (column names).
- `col1;col2;...` → list of column names to sort by (like SQL `ORDER BY`).

Example:

```bash
java TriExterne communes.csv REG;COM
```

This sorts the **communes.csv** dataset by region, then by commune name.

---

## 📌 Features

- Sorts CSV files on one or multiple columns (`ORDER BY`-like).
- Works with datasets **larger than memory**.
- Uses a memory cache of size `M` (tunable).
- Generates sorted fragments and merges them iteratively.
- Handles **lexicographic multi-column ordering**.
- Extendable to support **numeric vs textual column types**.

---

## 🏷️ Classes and Methods (Public API)

### **TriExterne**

Main class implementing the external sort algorithm.

- **TriExterne(String path, String colonnes, int M)** → Constructor, initializes cache, comparator, and CSV reader.
- **void trier()** → Runs the external sort: creates fragments, merges them until the file is fully sorted.
- **int creerFragmentsInitiaux()** → Generates sorted initial fragments from the input CSV.
- **int fusion(int niveau, int nombre)** → Merges up to `M-1` fragments into a higher-level fragment.
- **void testComparateur()** → Tests the comparator with sample tuples.

---

### **Comparateur**

Comparator for CSV tuples.

- **Comparateur(int[] indices)** → Constructor, selects which columns are used for sorting.
- **int compare(String[] t1, String[] t2)** → Compares two tuples lexicographically on the chosen columns.

---

## 🎓 Educational Context – What I Learned

This project was developed in the context of the **Advanced Databases** course (Université Clermont Auvergne, 2025).  
It helped me to better understand several **database system concepts**:

- How a DBMS performs **sorting operations** (`ORDER BY`, `GROUP BY`, `DISTINCT`) using external sort.
- The importance of **memory constraints** in real-world systems and how to design algorithms that scale beyond available RAM.
- The **two-phase sort-merge algorithm**: run generation and multi-way merging.
- How to design and implement a **comparator** for multi-column ordering, similar to SQL’s behavior.
- The role of **temporary files and disk I/O** in database query processing.

Overall, this assignment bridged **theory and practice** by turning abstract course concepts into a concrete Java program that manipulates real data.

---

## 🚀 Possible Extensions

- **Column typing**: allow specifying numeric (`NUM`) or textual (`TXT`) columns.
- **Heap optimization**: replace array-based cache with a priority queue for efficient merging.

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.
