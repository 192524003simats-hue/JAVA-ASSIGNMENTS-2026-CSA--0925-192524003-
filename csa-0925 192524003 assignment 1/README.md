# CSA09 - Assignment 1: Campus Smart-Parking System

**Student:** Hariharan *(confirm spelling before submission)*  
**Register number:** 192524003  
**Java version:** Java 17 or later (developed and checked with Java 27)  
**GitHub repository:** `<add your working GitHub repository URL here>`

This repository contains the report for Tasks 1–12, both completed-code exercises,
and an executable smart-parking implementation. The original assignment PDF is
kept at the repository root.

## Project layout

```text
Task01/ ... Task12/       Task evidence and pointers
Task03/                  Completed ParkingFacility exercise
Task08/                  Completed FeeDemo exercise
Task11-12/src/            Complete system and acceptance checks
REPORT.md                 Analysis, design, pseudocode, tests and complexity
```

## Compile and run

Run these commands from this directory in PowerShell:

```powershell
javac -d Task03\out Task03\ParkingFacility.java
java -cp Task03\out ParkingFacility

javac -d Task08\out Task08\FeeDemo.java
java -cp Task08\out FeeDemo

javac -d Task11-12\out (Get-ChildItem Task11-12\src -Recurse -Filter *.java).FullName
java -cp Task11-12\out parking.app.ParkingDemo
java -ea -cp Task11-12\out parking.app.AssignmentChecks
```

The checks are plain Java assertions; `-ea` enables them. No third-party
libraries are required.

## Task results

| Task | Result |
|---|---|
| 1–2 | Problem, objects, data types, jagged-array representation and trade-offs are documented in `REPORT.md`. |
| 3 | Allocation/release pseudocode and the completed `ParkingFacility` exercise are included. |
| 4–10 | Object model, constructors, static state, inheritance, overriding, polymorphism, abstraction, packages and encapsulation are documented and demonstrated in source. |
| 11 | The integrated model runs normal, boundary, invalid-input and daily-total checks. |
| 12 | The revised model supports e-bikes, delivery vans, visitor day passes, charging bays and unequal floor sizes. |

## Before submission

Confirm your name, add a real GitHub URL, create the required
`CSA09-192524003-Assignment1` repository, and progressively commit each task.
Run the commands above and capture your own output screenshots in the relevant
task folders. Do not claim a GitHub submission until the repository is actually
created and shared with your faculty.
