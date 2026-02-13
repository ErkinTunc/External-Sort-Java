# External Sort (Tri Externe) -- Large CSV Processing in Java

Java implementation of an external sorting algorithm designed to process
datasets larger than available memory, using disk-based run generation
and multi-way merging.

This project reproduces how database systems implement operations such
as:

- ORDER BY
- GROUP BY
- DISTINCT

when data cannot fit entirely into RAM.

---

## Problem Addressed

Standard in-memory sorting fails when datasets exceed memory limits.\
This implementation performs sorting using:

1.  Memory-sized sorted runs
2.  Disk storage of intermediate fragments
3.  Multi-pass merging strategy

allowing scalable processing of large CSV files.

---

## Algorithm Overview

### Phase 1 --- Run Generation

- CSV file is read block-by-block (size `M`).
- Each block is sorted in memory.
- Sorted blocks are stored as temporary fragments on disk.

### Phase 2 --- Multi-way Merge

- Up to `M-1` fragments are merged simultaneously.
- Merging continues level by level.
- Execution ends when a single sorted fragment remains.

---

## Technical Characteristics

- External sorting using disk fragments
- Multi-column CSV ordering
- Automatic numeric/text comparison
- Memory-constrained processing
- Multi-pass merge strategy
- Temporary fragment cleanup
- UTF-8 file handling
- Command-line execution support

---

## Project Structure

    src/tri_externe/
     ├── TriExterne.java   # External sorting engine
     └── Comparateur.java  # Multi-column comparator

    data/                  # Input datasets
    tmp/                   # Generated fragments
    output/                # Final sorted file

---

## Compilation

```bash
javac src/tri_externe/*.java
```

---

## Execution

Sort by columns:

```bash
java -cp src tri_externe.TriExterne data/communes.csv "REG;COM"
```

Specify column types:

```bash
java -cp src tri_externe.TriExterne data/communes.csv "REG;COM" "TXT;TXT"
```

PowerShell users must quote arguments containing `;`.

---

## Output

The final sorted file is written to:

    output/sorted.csv

Temporary fragments are automatically cleaned.

---

## Skills Demonstrated

This project demonstrates:

- Large-scale data processing
- External memory algorithms
- Efficient disk I/O handling
- Comparator design similar to SQL ordering
- Multi-way merge implementation
- Resource-aware algorithm design

---

## Academic Context

Developed during the Advanced Databases course (Université Clermont
Auvergne, 2025), bridging theoretical database algorithms with practical
implementation.

---

## Possible Extensions

- Heap-based k-way merge optimization
- Parallel merging
- Dynamic memory sizing
- Streaming input support
- Index-based sorting
