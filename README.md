# External Sort (Tri Externe) in Java

This repository contains a Java implementation of the **external sorting algorithm** ("Tri Externe") as part of a database systems course assignment (TP2).

External sort is used when the dataset is **too large to fit into memory (RAM)**.  
The algorithm works in two main phases:

1. **Run generation:** The input file is read in blocks of size `M` (the memory cache). Each block is sorted and written to disk as a temporary fragment.
2. **Multi-way merge:** The sorted fragments are merged together (using up to `M-1` fragments simultaneously) until a single sorted output file remains.

---

## 📌 Features

- Sorts CSV files based on one or multiple columns.
- Memory-limited: only `M` rows are stored in RAM at once.
- Generates intermediate sorted fragments on disk.
- Performs multi-level **k-way merge** until a final sorted file is obtained.
- Custom comparator for multi-column lexicographic order.

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

- `input.csv` → CSV file with header (first line = column names).
- `col1;col2;...` → list of column names to sort by (in order).

Example:

```bash
java TriExterne data.csv name;age
```

---

## 📂 Project Structure

- `TriExterne.java` → Main class, manages external sort flow.
- `Comparateur.java` → Comparator for sorting rows by given columns.
- `ArbreB.java` (optional, from TP1) → Not directly required here.

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.
