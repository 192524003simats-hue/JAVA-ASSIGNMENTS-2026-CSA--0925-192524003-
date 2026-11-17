# Assignment 1 Report: Campus Smart-Parking System

**Student:** Hariharan *(confirm the spelling shown on the college register)*  
**Register number:** 192524003  
**Course outcome:** CO1 — Java fundamentals and object-oriented programming  
**GitHub repository:** `<insert the working repository URL after publishing>`

## Design assumptions

1. Registration numbers identify vehicles and are normalized to uppercase. One
   vehicle can have at most one active ticket in a facility.
2. The system uses the campus local time, and each started parking hour is
   charged as a full hour. A zero-minute stay has no parking charge.
3. Rates used by the executable model are Rs 10/hour for two-wheelers,
   Rs 30/hour for cars, Rs 25 per e-bike visit, and Rs 12/kWh for charging.
   The assignment does not prescribe rates or pass prices; these are explicit
   illustrative assumptions, not university policy.
4. A delivery van's billable hour is charged at twice the car rate if that
   hour begins from 9:00 (inclusive) to 17:00 (exclusive); outside that time
   it is charged at the car rate. If an hour crosses a boundary, its rate is
   determined by the start of that billable hour.
5. A valid monthly pass covers a stay only when both entry and exit are in its
   month. A visitor's one-day pass covers a stay only when both timestamps are
   on its date. Passes waive parking fees, not EV electricity charges.
6. EVs prefer an unoccupied charging bay, but can use a standard bay when all
   charging bays are occupied. Non-EVs cannot take charging-only bays.
7. All monetary values in the exercises are whole-rupee values. `double` is
   retained to match the supplied FeeDemo; a production billing system should
   use `BigDecimal` or integer paise.

## Task 1 — Problem analysis and objects (4 marks)

**Inputs:** facility floor capacities and charging-bay layout; vehicle
registration/type; entry and exit timestamps; pass validity; and charging
energy consumed. **Outputs:** ticket number, assigned floor/slot, duration,
parking fee, charging fee, and available/occupied state. Invalid types,
duplicate active registrations, invalid times, or a full facility must produce
an explicit error rather than a misleading successful result.

| Candidate object | Why it is needed / responsibility |
|---|---|
| `ParkingFacility` | Owns the floors and active-ticket index; allocates compatible bays and coordinates entry/exit. |
| `ParkingSlot` | Represents one physical place, its coordinates, charging capability, and current occupant; enforces single occupancy. |
| `Vehicle` | Abstract shared identity and pricing contract; normalizes and protects registration data. |
| Vehicle subtypes | Express differences/capabilities: `TwoWheeler`, `Car`, `ElectricCar`, `EBike`, `DeliveryVan`; EVs implement `Chargeable`. |
| `ParkingTicket` | Immutable evidence of one entry: globally unique number, vehicle, slot, and entry time. |
| `ParkingPass` | Represents a fee-waiver capability independently from a vehicle type; monthly and one-day implementations check validity. |
| `PricingRule` | Separates vehicle identity from rate policy; hourly, flat, and time-dependent van strategies implement it. |
| `ParkingReceipt` | Immutable exit result with minutes, parking charge, charging charge, and total. |
| `VehicleFactory` | Converts an external type label to a supported subtype and rejects unknown labels. |

Important verbs become `park`, `exit`, `calculateFee`, `allocate`, `release`,
`covers`, and `chargingFee`. Invariants: one occupant per slot; one active
ticket per registration; ticket numbers are never reused; a vehicle cannot
exit before entry; a charging bay is restricted to chargeable vehicles; and
invalid negative minutes/kWh are rejected.

**Alternative — parallel arrays:** `regNo[]`, `type[]`, and `entryTime[]`
keep related values at matching indexes. Searching a registration is O(n) in
either design, but arrays require maintaining the same index and coordinating
insert/delete operations across every array. With vehicle objects, one
reference carries all attributes and adding a field is localized to the
vehicle model/constructors. Both store O(n) records. A String reference in an
array is typically 4 or 8 bytes plus the String object; an object-reference
array has a similar reference cost plus one Vehicle object/header and its
fields. Exact byte counts depend on JVM layout and are not claimed here.

## Task 2 — Data representation (3 marks)

| Value | Java representation | Reason |
|---|---|---|
| Floor/slot index and floor capacity | `int` | Zero-based indexes and bounded array sizes. |
| Registration and ticket number | `String` | Registration can contain letters and leading zeroes; tickets have a prefix. |
| Entry/exit time | `LocalDateTime` | Readable date/time comparisons and duration calculation. |
| Duration | `long` from `Duration.toMinutes()`, narrowed to `int` only after a range check | Avoids overflow while measuring a stay. |
| Rate, fee, kWh | `double` in this educational program; `int` for kWh | Assignment sample expects double output. Use decimal/integer-minor-units for real accounting. |
| Floor structure | Jagged `ParkingSlot[][]` | Each floor may have a different capacity and each element carries slot state. |

Charging slots are supplied as a matching jagged boolean layout. No unsafe
numeric cast is needed to bill hours: `minutes / 60 + (minutes % 60 == 0 ? 0 :
1)` performs a ceiling division using integer arithmetic. For example,
`130 / 60` is `2`, not `3`, so charging `2 * Rs 30` would underbill by Rs 30.
The remainder check yields three started hours and Rs 90. Casting after
integer division, `(double) (130 / 60)`, still gives `2.0`; cast before
division, `130 / 60.0`, yields approximately `2.167`, but must still be
rounded up to a whole billable hour.

This project uses method arguments rather than interactive `Scanner` input so
tests are repeatable. A console front end could use `Scanner` and validate
each token before calling the service.

**Alternative structures:** A flat `String[]`/slot array maps
`index = floor * slotsPerFloor + slot` in O(1) only when every floor has the
same capacity; unequal floors need prefix offsets or a scan to translate
coordinates. It stores O(S) entries. A jagged array supports unequal lengths
directly, uses O(F+S) references (row arrays plus slot entries), and accesses
a known coordinate in O(1). `ArrayList<String[]>` has the same O(1) indexed
access and O(F+S) storage, but brings resizable-list overhead; it is useful
when the number of floors changes dynamically, which this model does not need.

## Task 3 — Control logic and pseudocode (4 marks)

**Allocation pseudocode**

```text
START Allocate(vehicle)
  IF vehicle is already registered as active THEN
    REPORT duplicate-registration error
  END IF
  IF vehicle is chargeable THEN
    FOR each floor from first to last
      FOR each slot from first to last on that floor
        IF slot is free AND slot is a charging slot THEN
          OCCUPY slot; CREATE ticket; RETURN ticket
        END IF
      END FOR
    END FOR
  END IF
  FOR each floor from first to last
    FOR each slot from first to last on that floor
      IF slot is free AND slot is not charging-only THEN
        OCCUPY slot; CREATE ticket; RETURN ticket
      END IF
    END FOR
  END FOR
  REPORT facility-full error
END Allocate
```

**Fee and exit pseudocode**

```text
START Exit(registration, exitTime, kWh)
  ticket <- FIND active ticket by normalized registration
  IF ticket does not exist THEN REPORT unknown-ticket error
  IF exitTime < ticket.entryTime THEN REPORT invalid-time error
  IF kWh < 0 OR (kWh > 0 AND vehicle is not chargeable)
    THEN REPORT invalid-energy error
  minutes <- whole elapsed minutes
  IF minutes = 0 THEN parkingFee <- 0
  ELSE
    billableHours <- minutes DIV 60
    IF minutes MOD 60 > 0 THEN billableHours <- billableHours + 1
    parkingFee <- vehicle pricing rule(billableHours, entryTime)
    IF an applicable pass covers entry and exit THEN parkingFee <- 0
  END IF
  chargingFee <- kWh * charging rate for a chargeable vehicle; otherwise 0
  total <- parkingFee + chargingFee
  RELEASE ticket.slot; REMOVE active ticket
  RETURN receipt(total, parkingFee, chargingFee)
END Exit
```

The loops stop immediately on the first suitable free slot (`return`); the
other floors/slots are not searched. `continue` is not required in the Java
implementation because an `if` around the work is clearer. A full facility
raises `IllegalStateException`. The completed standalone exercise in
`Task03/ParkingFacility.java` searches floor/slot order and returns `null`
when full; it validates coordinates before release.

**Alternative free-slot stack:** Maintain a stack of free coordinates per
floor. Allocation pops a suitable coordinate and release pushes it back, so
both operations are O(1) amortized, compared with O(S) worst-case scanning
for the supplied first-free search (F floors, S total slots). A count alone
cannot identify a free slot, so it cannot make allocation O(1) without an
additional index/stack. The stacks add O(S) coordinate storage; the scan
needs only O(1) extra working space. The stack approach must also maintain
separate compatible queues for standard and charging slots.

## Task 4 — Classes, objects, and constructors (4 marks)

Constructors establish nonblank normalized registration numbers, a valid
pricing rule, slot dimensions, and immutable ticket fields. Vehicle
constructors are overloaded with and without a pass; a no-pass constructor
chains to the common constructor using `this(...)` / `super(...)`. `ParkingSlot`
and `ParkingTicket` use parameterized constructors so incomplete objects are
not exposed. A copy constructor is not appropriate for a ticket: copying one
would risk duplicating a unique ticket number and active parking state. A
reprint should return the existing ticket.

The local reference variable is typically on a stack frame; the referenced
vehicle, ticket and slot objects live on the heap. Maps/arrays store
references, not inlined objects. After exit removes the map's last active
reference, the ticket and its vehicle are eligible for garbage collection if
no caller retains them. The immutable receipt retains its ticket for the
transaction result.

**Alternative creation methods:** A public no-argument constructor followed
by setters permits partially initialized or invalid objects and requires
validation on each setter. A factory method can validate and select
subclasses, as `VehicleFactory` does. For n objects, all approaches require
O(n) object memory and O(n) construction work; method-call overhead is O(1)
per setter/factory and does not change asymptotic cost. Constructors/factories
offer a stronger valid-object invariant.

## Task 5 — Static members and object life cycle (3 marks)

`ParkingTicket.NEXT_NUMBER` is a static `AtomicLong`: the sequence belongs to
all tickets, not one ticket or one facility. Its atomic increment also avoids
duplicate numbers when concurrent facilities issue tickets. Each ticket
stores its generated number as an instance field. A static factory method
(`VehicleFactory.create`) is appropriate because it creates a subtype without
requiring a factory object. A static initializer is unnecessary for fixed
compile-time rates.

An instance counter in each facility would use O(k) counters across k
facilities and would allow duplicated IDs unless the facility identifier is
part of the ticket. One global static counter uses O(1) counter space for n
tickets and O(n) ticket objects. In a distributed deployment, a database
sequence or facility-prefix plus local sequence is preferable.

Methods receive and return object references; Java passes the reference value
by value. After successful exit, the active map entry and slot's vehicle
reference are removed, making active ticket/vehicle objects collectible when
no other reference remains.

## Task 6 — Inheritance hierarchy (4 marks)

```text
                         Vehicle (abstract)
                       /       |          \
             TwoWheeler       Car         EBike
                              /  \
                     ElectricCar DeliveryVan

ElectricCar implements Chargeable
ParkingPass is an independent interface implemented by MonthlyPass / OneDayPass
```

The primary hierarchy is hierarchical/single inheritance: the abstract base
holds registration and pass state; each type supplies or selects pricing.
`Car` passes its registration/pass to `Vehicle` with `super(...)`; `ElectricCar`
and `DeliveryVan` call `super(...)` in `Car`. When an `ElectricCar` is
constructed, execution is `Vehicle` field initialization -> `Vehicle`
constructor -> `Car` field initialization -> `Car` constructor ->
`ElectricCar` field initialization -> `ElectricCar` constructor. If the
superclass constructor calls an overridable method, Java can dispatch to the
subclass before its fields are initialized; this design avoids that pattern.

**Composition alternative:** A `Vehicle` would hold a `VehicleType` / pricing
strategy object. Adding a category would add a strategy and factory mapping
rather than a subclass, useful for types/capabilities that change at runtime.
Both approaches call a pricing operation in O(1) for hourly/flat pricing; this
delivery-van rule iterates H billable hours, O(H). A composition design adds
one strategy reference per vehicle (O(1) extra space), while inheritance
uses the class identity/vtable already associated with the object. The
implemented design combines subtype identity with a `PricingRule` strategy,
keeping policy separate and extensible.

## Task 7 — Overriding and specialization (4 marks)

`Vehicle.equals`, `hashCode`, and `toString` override `Object` methods.
Subclasses provide pricing behavior by overriding `pricingRule()` and
`DeliveryVan` specializes the car rate strategy. `@Override` catches an
incorrect method signature at compile time. An override must retain the
signature and compatible covariant return type, and cannot reduce visibility.
`final` is used for immutable registration, ticket number, slot coordinates,
rates, and classes/methods where identity/invariants must not be changed.

Equality uses normalized registration, so two objects of different runtime
vehicle classes with the same registration compare equal; `hashCode` uses the
same field. This is correct for a system where registration uniquely
identifies one vehicle, and the active-ticket map enforces that uniqueness.
If equality represented subtype-specific value objects instead, runtime
class would need to be included consistently in both methods.

**Switch alternative:** A `calculateFee` method could switch on an enum type
and apply each rate. In a linear if/else chain, up to k type checks are needed
(O(k)); each new type requires editing the central switch and testing it. A
modern enum switch may compile efficiently, but it still centralizes policy
and creates source-level coupling. The polymorphic pricing call is O(1) for
hourly/flat rules; adding a class and rule avoids expanding the existing fee
method.

## Task 8 — Polymorphism (4 marks)

Compile-time polymorphism is demonstrated by
`calculateFee(int minutes)` and
`calculateFee(LocalDateTime entry, int minutes)`; argument count/types select
the overload during compilation. Runtime polymorphism is demonstrated by
calling `pricingRule()` through a `Vehicle` reference: an `ElectricCar` is a
`Car`, but its actual pricing implementation is selected from its runtime
class. For the standalone `FeeDemo`, the declared type in `for (Vehicle v :
parked)` is `Vehicle`, actual types are `TwoWheeler`, `Car`, and
`ElectricCar`, and `v.calculateFee(130)` dispatches to the inherited method,
which calls the relevant `ratePerHour()`.

The one-array approach avoids a long call-site type switch. A direct virtual
dispatch is O(1) for the call (and the arithmetic is O(1)); checking each of k
`instanceof` types can take O(k). The standalone exercise outputs the
provided expected fees: Rs 30, Rs 90, Rs 210, then `true`. `Car("TN02B2")`
equals `ElectricCar("TN02B2")` because equality uses registration only. That
is suitable only when registration is the unique vehicle identity, as in
this model.

## Task 9 — Abstraction (4 marks)

`Vehicle` is abstract because a generic vehicle has no single meaningful
pricing policy; its protected `pricingRule()` contract is supplied by concrete
types. `Chargeable` is an interface because charging is a capability that
can be added to vehicles independently of their class lineage. `ParkingPass`
and `PricingRule` also use interfaces to let independent classes implement
fee-waiver and pricing behavior. `ParkingPass` has a default method applying
the standard waiver policy and a static `isCovered` helper for optional passes.

**Swapped design:** Making `Vehicle` an interface would remove its constructor
and shared per-object registration/pass fields; every implementation would
duplicate that state or delegate to another object. Making `Chargeable` an
abstract class would prevent a vehicle that already extends `Car` from
extending it too, since Java has single class inheritance. Interfaces add no
per-object fields (only constants); object footprint and method dispatch
asymptotics remain O(1), while abstract-class state occupies its normal
instance fields.

## Task 10 — Packages and encapsulation (3 marks)

| Package | Contents |
|---|---|
| `parking.model` | Vehicle hierarchy, slots, tickets, passes, fees, receipts, factory |
| `parking.service` | Facility allocation and active ticket lifecycle |
| `parking.app` | Runnable demo and acceptance checks |

Fields are private; public methods expose validated operations. Subclass
hooks and rates use `protected` only where inheritance requires access.
`ParkingFacility` operations that mutate shared state are synchronized.
Vehicle registration is normalized/validated in the constructor, slot layout
is validated when the facility is built, and negative duration/energy and
invalid exits are rejected. The active ticket map uses `String` keys, avoiding
wrapper identity pitfalls. `AtomicLong` returns a primitive `long`; if an
`Integer`/`Long` wrapper is used as a value, compare values with `equals` or
primitive conversion, not `==`.

If fields were public, each caller would have to repeat validation on every
write. Validation itself is O(1), but with m client sites the same check may
be repeated O(m) times and one missed check can corrupt state. Encapsulation
centralizes each update check and keeps total update complexity O(1).

## Task 11 — Implementation, tests, and evaluation (5 marks)

The integrated model is in `Task11-12/src`. Active tickets are indexed by
registration in `HashMap<String, ParkingTicket>`: average O(1) lookup/insert/
remove, O(n) worst case under pathological collisions, and O(n) references
for n parked vehicles. The completed assignment's standalone `String[][]`
finder uses O(n) search time and O(n) slot storage. An `ArrayList` scan is
also O(n) to find a registration; it uses O(n) references but deletion shifts
up to O(n) entries. A HashMap uses O(n) entries and additional hash-table
capacity overhead. These are asymptotic comparisons; no JVM microbenchmark
has been performed.

Run `parking.app.AssignmentChecks` with assertions enabled:

```text
PASS: 28 acceptance checks
```

| Test | Expected | Actual |
|---|---|---|
| Car, 0 minutes | Rs 0 | Rs 0 |
| Car, exactly 60 minutes | Rs 30 | Rs 30 |
| Car, 61 minutes | Rs 60 | Rs 60 |
| Car, 130 minutes | Rs 90 | Rs 90 |
| Allocation across unequal floors / full facility | First available slot; next entry rejected | Pass |
| Exit releases active ticket; ticket IDs differ | Count decreases; distinct IDs | Pass |
| EV bay choice, 90 minutes plus 10 kWh | Charging bay; Rs 60 parking + Rs 120 charging | Rs 180 |
| Visitor day pass / monthly pass | Parking waived when valid | Rs 0 |
| E-bike, positive-duration visit | Flat Rs 25 | Rs 25 |
| Delivery van at 10 AM for one hour | Double car rate | Rs 60 |
| Van from 8–10 AM | Rs 30 + Rs 60 | Rs 90 |
| Duplicate reg, unknown type, reverse time, non-EV charging, unknown ticket | Explicit rejection | Pass |
| Invalid floor/slot and malformed bay layout | Invalid indexes/layout rejected | Pass |
| Daily scenario: 2-hour car + 1-hour two-wheeler | Rs 60 + Rs 10 = Rs 70 | Rs 70 |
| Same-reg `Car` and `ElectricCar` equality/hash | Equal; one HashSet entry | Pass |

For the hand calculation, the visitor car parks for 120 minutes:
`ceil(120/60) * Rs 30 = Rs 60`. The two-wheeler parks for 60 minutes:
`1 * Rs 10 = Rs 10`. Daily collection is Rs 70, matching the test. The
design demonstrates encapsulation, inheritance, runtime polymorphism,
interfaces and abstraction. One limitation is that this educational model is
in-memory and not persistent across process restarts; production concurrency,
time-zone policy, tax, receipts and monetary precision need further design.

## Task 12 — Re-evaluate and adapt the design (8 marks)

### A. Impact analysis

The changed requirements add e-bikes with flat pricing, delivery vans with a
9 AM–5 PM price window, visitor one-day passes, and five floors with
different capacities. Unequal capacity requires a jagged array, not a
rectangular `floor x slotsPerFloor` assumption. The facility's allocation
policy now also distinguishes charging and standard bays. New classes are
`EBike`, `DeliveryVan`, `FlatPricing`, `DeliveryVanPricing`, and `OneDayPass`;
the factory and facility are extended to recognize types and passes.

### B. Evaluation

The basic inheritance model can add categories as subclasses, but adding every
pricing exception directly to subclass `calculateFee()` methods spreads
policy across classes. The revised design uses the `PricingRule` abstraction
to keep hourly, flat, and time-dependent rates separate. Registration,
ticket, slot, and facility concepts remain; the facility needs modification
for unequal layouts and charging-bay preference, and the pricing/vehicle
extension points need the strategy contract. No claim is made that an
existing facility implementation remains unchanged.

### C. Adaptation

**Decision: modify the design.** Keep `Vehicle`, `ParkingSlot`,
`ParkingTicket`, and a jagged floor representation. Add independent
`PricingRule` and `ParkingPass` interfaces, new rule/pass implementations,
the two vehicle classes, a factory mapping, and charging-bay metadata.
`DeliveryVanPricing` evaluates each billable hour; `OneDayPass` checks the
entry and exit date. An additional vehicle type can now be added with a
subclass and pricing rule without rewriting the allocation algorithm.

### D. Evidence-based defence

Relative to the original proposed classes, four core surfaces need changes
for the challenge: `Vehicle` gains the pricing strategy hook, `Car` exposes
its base rate, `ParkingTicket`/pass handling carries pass state, and
`ParkingFacility` accepts jagged charging layouts and performs compatible
allocation. Two new vehicle categories and independent pricing/pass classes
are additions. The acceptance checks validate both new category rules, the
day pass, unequal floor capacity, and EV charging allocation. This trades a
few more small classes and configuration objects for a central allocation
algorithm that does not need a new conditional for every future vehicle.
