# Tasks 11–12 — Full system

The complete model, demo, and dependency-free acceptance checks are in `src`.
Task 11 evaluates the baseline operations and test cases; Task 12 extends the
same model with e-bikes, delivery vans, visitor day passes, charging bays, and
unequal floor sizes. The written evidence is in `../REPORT.md`.

Compile and run from the assignment root in PowerShell:

```powershell
javac -d Task11-12\out (Get-ChildItem Task11-12\src -Recurse -Filter *.java).FullName
java -cp Task11-12\out parking.app.ParkingDemo
java -ea -cp Task11-12\out parking.app.AssignmentChecks
```

`Output.txt` records the successful output. It is not a screenshot; capture
your own terminal screenshots after running the commands if your faculty
requires screenshots.
