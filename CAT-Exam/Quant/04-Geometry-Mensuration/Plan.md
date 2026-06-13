# Geometry and Mensuration - Complete CAT Preparation Guide

## Why This Topic Matters for CAT
Geometry typically has 4-6 questions in CAT. Many students avoid it, but if you know the
key theorems and formulas, these are easy marks. Mensuration (3D shapes) is formula-heavy
but straightforward once memorized.

---

## 1. Triangles

### Basic Properties

- **Sum of angles = 180 degrees**
- **Exterior angle = Sum of two non-adjacent interior angles**
- **Triangle inequality**: Sum of any two sides > Third side
  - Also: Difference of any two sides < Third side
  - **When to use**: "Is a triangle possible with sides a, b, c?"

### Types of Triangles

| Type | Property | Area Formula |
|------|----------|-------------|
| Equilateral | All sides equal, all angles = 60 degrees | (sqrt(3)/4) x a^2 |
| Isosceles | Two sides equal, two angles equal | (b/4) x sqrt(4a^2 - b^2) where b is the unequal side |
| Right | One angle = 90 degrees | (1/2) x base x height |
| Scalene | All sides different | Use Heron's formula |

### Area Formulas for Triangles

**Formula 1: Basic**
Area = (1/2) x base x height
- **When to use**: When base and perpendicular height are known

**Formula 2: Heron's Formula**
- s = (a + b + c) / 2 (semi-perimeter)
- Area = sqrt[s(s-a)(s-b)(s-c)]
- **When to use**: When all three sides are known but height isn't

**Formula 3: Trigonometric**
Area = (1/2) x a x b x sin(C)
- **When to use**: When two sides and the included angle are known

**Formula 4: Equilateral Triangle**
Area = (sqrt(3)/4) x a^2
Height = (sqrt(3)/2) x a
- **When to use**: Any equilateral triangle problem. MEMORIZE these!

**Formula 5: Using coordinates** (covered in coordinate geometry section below)

### Pythagoras Theorem and Pythagorean Triplets

**a^2 + b^2 = c^2** (where c is the hypotenuse)

**Common triplets to MEMORIZE:**

| Triplet | Multiples you'll see |
|---------|---------------------|
| 3, 4, 5 | 6-8-10, 9-12-15, 12-16-20, 15-20-25 |
| 5, 12, 13 | 10-24-26 |
| 8, 15, 17 | - |
| 7, 24, 25 | - |
| 9, 40, 41 | - |
| 11, 60, 61 | - |
| 20, 21, 29 | - |

**Generating triplets**: For any m > n > 0:
- a = m^2 - n^2, b = 2mn, c = m^2 + n^2
- Example: m=2, n=1 gives 3, 4, 5
- Example: m=3, n=2 gives 5, 12, 13

**Acute vs Obtuse Triangle:**
- If c^2 < a^2 + b^2 -> Acute triangle
- If c^2 > a^2 + b^2 -> Obtuse triangle
- If c^2 = a^2 + b^2 -> Right triangle

### Similarity and Congruence

**Similarity (same shape, different size):**
Criteria: AA (Angle-Angle), SAS (Side-Angle-Side), SSS (Side-Side-Side with proportional sides)

**Key property of similar triangles:**
- Ratio of sides = k
- Ratio of perimeters = k
- Ratio of areas = k^2
- Ratio of volumes (3D) = k^3

**Congruence (same shape, same size):**
Criteria: SSS, SAS, ASA, AAS, RHS (for right triangles)

### Basic Proportionality Theorem (BPT) - Thales' Theorem

If a line is drawn parallel to one side of a triangle cutting the other two sides, it divides those sides proportionally.

If DE is parallel to BC in triangle ABC, where D is on AB and E is on AC:
- **AD/DB = AE/EC**
- Also: AD/AB = AE/AC = DE/BC

**When to use**: Any problem with a line parallel to one side of a triangle

### Angle Bisector Theorem

The angle bisector of an angle divides the opposite side in the ratio of the adjacent sides.

If AD bisects angle A in triangle ABC (D on BC):
- **BD/DC = AB/AC**

**When to use**: Questions involving angle bisectors

### Apollonius Theorem (Medians)

If AD is a median from A to side BC (D is midpoint of BC):
- **AB^2 + AC^2 = 2(AD^2 + BD^2)**
- Since BD = BC/2: **AB^2 + AC^2 = 2[AD^2 + (BC/2)^2]**

**When to use**: Finding the length of a median

**Length of median formula:**
- m_a = (1/2) x sqrt(2b^2 + 2c^2 - a^2)
- where a, b, c are sides and m_a is the median to side a

### Important Centers of a Triangle

**1. Centroid (G)** - Intersection of medians
- Divides each median in ratio 2:1 from vertex
- Coordinates: ((x1+x2+x3)/3, (y1+y2+y3)/3)
- Always inside the triangle

**2. Incentre (I)** - Intersection of angle bisectors
- Center of the inscribed circle (incircle)
- **Inradius r = Area / s** (where s = semi-perimeter)
- Equidistant from all three sides
- Always inside the triangle
- Coordinates: (ax1+bx2+cx3)/(a+b+c), (ay1+by2+cy3)/(a+b+c) where a,b,c are side lengths opposite vertices

**3. Circumcentre (O)** - Intersection of perpendicular bisectors
- Center of the circumscribed circle (circumcircle)
- **Circumradius R = abc / (4 x Area)**
- Equidistant from all three vertices
- For acute triangle: inside; for right triangle: midpoint of hypotenuse; for obtuse: outside

**4. Orthocentre (H)** - Intersection of altitudes
- For acute triangle: inside
- For right triangle: at the right-angle vertex
- For obtuse triangle: outside

**Important relationships:**
- G, O, H are collinear (Euler's line)
- G divides OH in ratio 1:2 (OG:GH = 1:2)
- For equilateral triangle: all four centers coincide!

### Special Triangle Properties

**30-60-90 triangle**: Sides are in ratio 1 : sqrt(3) : 2
- If shortest side = a, then sides are a, a*sqrt(3), 2a

**45-45-90 triangle**: Sides are in ratio 1 : 1 : sqrt(2)
- If legs = a, hypotenuse = a*sqrt(2)

**Area using inradius**: Area = r x s (r = inradius, s = semi-perimeter)
**Area using circumradius**: Area = abc / (4R)

---

## 2. Circles

### Basic Formulas

- **Area = pi x r^2**
- **Circumference = 2 x pi x r = pi x d** (d = diameter)

### Arc and Sector

- **Arc length = (theta/360) x 2 x pi x r** (theta in degrees)
- **Sector area = (theta/360) x pi x r^2**
- **Sector area = (1/2) x arc length x r**

**Segment area** (area between a chord and its arc):
- Segment = Sector area - Triangle area
- For minor segment: (theta/360) x pi x r^2 - (1/2) x r^2 x sin(theta)

### Tangent Properties

1. **Tangent is perpendicular to radius** at the point of tangency
2. **Two tangents from an external point are equal** in length
   - If PA and PB are tangents from P to circle with center O:
   - PA = PB
   - OP bisects angle APB
   - OP bisects angle AOB
3. **Tangent-secant relationship**: If from external point P, PT is tangent and PAB is secant:
   - **PT^2 = PA x PB**

### Chord Properties

1. **Perpendicular from center bisects the chord** (and vice versa)
2. **Equal chords are equidistant from center**
3. **Chord-chord intersection**: If two chords AB and CD intersect at P:
   - **PA x PB = PC x PD** (Power of a Point - internal)

### Inscribed Angle Theorem (VERY IMPORTANT)

- **Inscribed angle = (1/2) x Central angle** (subtending the same arc)
- Angle in a semicircle = 90 degrees (Thales' theorem)
- Angles in the same segment are equal
- Opposite angles of a cyclic quadrilateral sum to 180 degrees

### Power of a Point

For a point P and a circle:
- **If P is outside**: PA x PB = PC x PD (for any two secants)
  - Also: PT^2 = PA x PB (tangent-secant)
- **If P is inside**: PA x PB = PC x PD (for any two chords through P)

### Two Circles

**External tangent**: Length = sqrt[d^2 - (r1 - r2)^2] (d = distance between centers)
**Internal tangent**: Length = sqrt[d^2 - (r1 + r2)^2]

**Number of common tangents:**

| Condition | Common Tangents |
|-----------|----------------|
| d > r1 + r2 (no overlap) | 4 |
| d = r1 + r2 (external touch) | 3 |
| |r1 - r2| < d < r1 + r2 (intersecting) | 2 |
| d = |r1 - r2| (internal touch) | 1 |
| d < |r1 - r2| (one inside other) | 0 |

---

## 3. Quadrilaterals

### Properties and Area Formulas

**Parallelogram:**
- Opposite sides parallel and equal
- Opposite angles equal
- Diagonals bisect each other (but NOT necessarily equal)
- **Area = base x height**

**Rectangle:**
- All angles = 90 degrees
- Diagonals are equal and bisect each other
- **Area = length x breadth**
- **Diagonal = sqrt(l^2 + b^2)**

**Square:**
- All sides equal, all angles = 90 degrees
- Diagonals equal, bisect each other at 90 degrees
- **Area = side^2 = (diagonal^2) / 2**
- **Diagonal = side x sqrt(2)**

**Rhombus:**
- All sides equal
- Diagonals bisect each other at 90 degrees (but NOT equal)
- **Area = (1/2) x d1 x d2** (d1, d2 are diagonals)
- **Side = (1/2) x sqrt(d1^2 + d2^2)**

**Trapezium (Trapezoid):**
- One pair of parallel sides
- **Area = (1/2) x (sum of parallel sides) x height = (1/2) x (a + b) x h**

**Cyclic Quadrilateral:**
- All four vertices lie on a circle
- **Opposite angles sum to 180 degrees**
- Area = sqrt[(s-a)(s-b)(s-c)(s-d)] (Brahmagupta's formula, s = semi-perimeter)
- **Ptolemy's theorem**: AC x BD = AB x CD + AD x BC (product of diagonals = sum of products of opposite sides)

### Diagonal Properties Summary

| Shape | Diagonals bisect? | Diagonals equal? | Diagonals perpendicular? |
|-------|-------------------|------------------|------------------------|
| Parallelogram | Yes | No | No |
| Rectangle | Yes | Yes | No |
| Rhombus | Yes | No | Yes |
| Square | Yes | Yes | Yes |
| Trapezium | No | No | No |

---

## 4. Mensuration (3D Shapes)

### Cube

- **Volume = a^3**
- **Total Surface Area = 6a^2**
- **Lateral Surface Area = 4a^2**
- **Space diagonal = a x sqrt(3)**
- **Face diagonal = a x sqrt(2)**

### Cuboid (Rectangular Box)

- **Volume = l x b x h**
- **Total Surface Area = 2(lb + bh + hl)**
- **Lateral Surface Area = 2h(l + b)**
- **Space diagonal = sqrt(l^2 + b^2 + h^2)**

### Cylinder

- **Volume = pi x r^2 x h**
- **Curved Surface Area (CSA) = 2 x pi x r x h**
- **Total Surface Area = 2 x pi x r x (r + h)**

**Hollow cylinder:**
- Volume = pi x h x (R^2 - r^2) (R = outer, r = inner radius)
- CSA = 2 x pi x h x (R + r) ... actually CSA (outer + inner) = 2pi*h*(R+r)
- Total SA = 2pi*h*(R+r) + 2pi*(R^2 - r^2)

### Cone

- **Slant height l = sqrt(r^2 + h^2)**
- **Volume = (1/3) x pi x r^2 x h**
- **Curved Surface Area = pi x r x l**
- **Total Surface Area = pi x r x (r + l)**

**When a cone is unfolded**, it forms a sector:
- Sector radius = l (slant height)
- Sector arc length = 2 x pi x r (circumference of cone base)
- Sector angle = (r/l) x 360 degrees

### Sphere

- **Volume = (4/3) x pi x r^3**
- **Surface Area = 4 x pi x r^2**

**Hemisphere:**
- Volume = (2/3) x pi x r^3
- Curved SA = 2 x pi x r^2
- Total SA = 3 x pi x r^2 (curved + flat circle)

### Frustum (Truncated Cone)

A cone with the top cut off parallel to the base.
- r = top radius, R = bottom radius, h = height, l = slant height

- **l = sqrt[h^2 + (R - r)^2]**
- **Volume = (pi x h / 3) x (R^2 + r^2 + Rr)**
- **CSA = pi x (R + r) x l**
- **Total SA = pi x (R + r) x l + pi x R^2 + pi x r^2**

### Common Solid Conversion Problems (Melting and Recasting)

**Key principle: Volume is conserved when melting and recasting**

**Example**: A sphere of radius 6 cm is melted and recast into a cylinder of radius 3 cm. Find the height.
- Volume of sphere = Volume of cylinder
- (4/3) x pi x 6^3 = pi x 3^2 x h
- (4/3) x 216 = 9h
- 288 = 9h
- h = 32 cm

**Example**: A cylinder of radius 7 cm and height 10 cm is melted into cones of radius 3.5 cm and height 5 cm. How many cones?
- Volume of cylinder / Volume of one cone = number of cones
- (pi x 49 x 10) / ((1/3) x pi x 12.25 x 5) = 490 / 20.417 = 24 cones

### Surface Area to Volume Ratio

For a given volume, the sphere has the smallest surface area.
For a given surface area, the sphere has the largest volume.
This concept appears in optimization problems.

---

## 5. Coordinate Geometry

### Distance Formula

Distance between (x1, y1) and (x2, y2):
**d = sqrt[(x2-x1)^2 + (y2-y1)^2]**

### Section Formula

Point dividing line joining (x1,y1) and (x2,y2) in ratio m:n:

**Internal division**: ((mx2 + nx1)/(m+n), (my2 + ny1)/(m+n))
**External division**: ((mx2 - nx1)/(m-n), (my2 - ny1)/(m-n))

### Midpoint Formula (Special case m:n = 1:1)

**Midpoint = ((x1+x2)/2, (y1+y2)/2)**

### Slope

**Slope m = (y2 - y1) / (x2 - x1)**

**Properties:**
- Parallel lines: m1 = m2
- Perpendicular lines: m1 x m2 = -1
- Horizontal line: m = 0
- Vertical line: m = undefined

### Equation of a Line

**Slope-intercept form**: y = mx + c (m = slope, c = y-intercept)
**Point-slope form**: y - y1 = m(x - x1)
**Two-point form**: (y - y1)/(y2 - y1) = (x - x1)/(x2 - x1)
**General form**: ax + by + c = 0 (slope = -a/b, y-intercept = -c/b)
**Intercept form**: x/a + y/b = 1 (a = x-intercept, b = y-intercept)

### Distance of Point from Line

Distance of point (x1, y1) from line ax + by + c = 0:

**d = |ax1 + by1 + c| / sqrt(a^2 + b^2)**

**When to use**: Finding altitude length, distance between parallel lines

**Distance between parallel lines** ax + by + c1 = 0 and ax + by + c2 = 0:
**d = |c1 - c2| / sqrt(a^2 + b^2)**

### Area of Triangle Using Coordinates

Given vertices (x1,y1), (x2,y2), (x3,y3):

**Area = (1/2) |x1(y2-y3) + x2(y3-y1) + x3(y1-y2)|**

**Shoelace formula** (same thing, easier to remember):
- Write coordinates in a column, repeat the first at the bottom:
  ```
  x1  y1
  x2  y2
  x3  y3
  x1  y1
  ```
- Sum of diagonal products going down-right: x1*y2 + x2*y3 + x3*y1
- Sum of diagonal products going down-left: y1*x2 + y2*x3 + y3*x1
- Area = (1/2)|first sum - second sum|

### Important Coordinate Geometry Facts

- **Collinear points**: Area of triangle = 0
- **Centroid** of triangle = ((x1+x2+x3)/3, (y1+y2+y3)/3)
- **Area of quadrilateral** with vertices: split into two triangles and add

### Equation of a Circle

**(x - h)^2 + (y - k)^2 = r^2** (center (h,k), radius r)

General form: x^2 + y^2 + 2gx + 2fy + c = 0
- Center = (-g, -f)
- Radius = sqrt(g^2 + f^2 - c)

---

## 6. Practice Problems with Solutions

### Problem 1
**In a right triangle, the hypotenuse is 25 cm. If one side is 7 cm, find the other side and the area.**

**Solution:**
Recognize: 7-24-25 is a Pythagorean triplet!
Other side = **24 cm**
Area = (1/2) x 7 x 24 = **84 sq cm**

Without recognizing: sqrt(25^2 - 7^2) = sqrt(625-49) = sqrt(576) = 24

### Problem 2
**Find the area of an equilateral triangle with side 10 cm.**

**Solution:**
Area = (sqrt(3)/4) x 10^2 = (sqrt(3)/4) x 100 = **25*sqrt(3) sq cm**
Approximately = 25 x 1.732 = 43.3 sq cm

### Problem 3
**A triangle has sides 13, 14, 15. Find its area.**

**Solution using Heron's formula:**
s = (13+14+15)/2 = 21
Area = sqrt[21 x (21-13) x (21-14) x (21-15)]
= sqrt[21 x 8 x 7 x 6]
= sqrt[7056]
= **84 sq cm**

Shortcut: 21 x 8 = 168, 7 x 6 = 42. 168 x 42 = 7056. sqrt(7056) = 84.

### Problem 4
**The circumference of a circle is 44 cm. Find the area of a sector with central angle 90 degrees.**

**Solution:**
2*pi*r = 44 -> r = 44/(2 x 22/7) = 44 x 7/44 = 7 cm
Sector area = (90/360) x pi x 7^2 = (1/4) x (22/7) x 49 = (1/4) x 154 = **38.5 sq cm**

### Problem 5
**A cone has radius 7 cm and slant height 25 cm. Find the volume.**

**Solution:**
h = sqrt(l^2 - r^2) = sqrt(625 - 49) = sqrt(576) = 24 cm
Volume = (1/3) x pi x r^2 x h = (1/3) x (22/7) x 49 x 24
= (1/3) x 22 x 7 x 24 = (1/3) x 3696 = **1232 cu cm**

### Problem 6
**Find the inradius of a triangle with sides 5, 12, and 13.**

**Solution:**
This is a right triangle (5-12-13 triplet)!
Area = (1/2) x 5 x 12 = 30
s = (5+12+13)/2 = 15
Inradius r = Area/s = 30/15 = **2 cm**

**Shortcut for right triangles**: r = (a + b - c)/2 where c is hypotenuse
r = (5 + 12 - 13)/2 = 4/2 = 2. Confirmed!

### Problem 7
**Two tangents are drawn from an external point to a circle of radius 5 cm. If the distance from the point to the center is 13 cm, find the length of each tangent.**

**Solution:**
Tangent is perpendicular to radius at the point of tangency.
Using Pythagorean theorem in the right triangle formed:
Tangent length = sqrt(13^2 - 5^2) = sqrt(169 - 25) = sqrt(144) = **12 cm**

### Problem 8
**A metallic sphere of radius 3 cm is melted and recast into 27 small spheres. Find the radius of each small sphere.**

**Solution:**
Volume of big sphere = 27 x Volume of small sphere
(4/3)*pi*3^3 = 27 x (4/3)*pi*r^3
27 = 27r^3
r^3 = 1
r = **1 cm**

### Problem 9
**In triangle ABC, D is on BC such that BD:DC = 3:4. If AB = 6, AC = 8, and BC = 10, is this a right triangle? Find AD.**

**Solution:**
Check: AB^2 + AC^2 = 36 + 64 = 100 = BC^2. YES, right triangle with right angle at A.

Wait: 6^2 + 8^2 = 100 = 10^2. Right angle at A.

BD = 3 x 10/7 = 30/7, DC = 40/7

Using Apollonius isn't directly applicable since D isn't the midpoint.
Use Stewart's theorem: AB^2 x DC + AC^2 x BD - AD^2 x BC = BC x BD x DC
36 x (40/7) + 64 x (30/7) - AD^2 x 10 = 10 x (30/7) x (40/7)
1440/7 + 1920/7 - 10AD^2 = 12000/49
3360/7 - 10AD^2 = 12000/49
23520/49 - 10AD^2 = 12000/49
10AD^2 = 11520/49
AD^2 = 1152/49
**AD = (24*sqrt(2))/7 cm**

### Problem 10
**Find the area of the triangle with vertices A(1, 2), B(4, 6), C(7, 2).**

**Solution:**
Area = (1/2)|x1(y2-y3) + x2(y3-y1) + x3(y1-y2)|
= (1/2)|1(6-2) + 4(2-2) + 7(2-6)|
= (1/2)|4 + 0 + (-28)|
= (1/2)|(-24)|
= **12 sq units**

### Problem 11
**A cylinder and a cone have the same base radius and the same height. If the volume of the cylinder is 36 cu cm, find the volume of the cone.**

**Solution:**
Volume of cone = (1/3) x Volume of cylinder (same base and height)
= (1/3) x 36 = **12 cu cm**

### Problem 12
**Find the distance from point (3, 4) to the line 3x + 4y - 5 = 0.**

**Solution:**
d = |3(3) + 4(4) - 5| / sqrt(9 + 16) = |9 + 16 - 5| / sqrt(25) = 20/5 = **4 units**

### Problem 13
**In a cyclic quadrilateral ABCD, angle A = 70 degrees. Find angle C.**

**Solution:**
In a cyclic quadrilateral, opposite angles are supplementary.
angle A + angle C = 180 degrees
angle C = 180 - 70 = **110 degrees**

### Problem 14
**A wire in the shape of a square of side 11 cm is reshaped into a circle. Find the area of the circle.**

**Solution:**
Perimeter of square = circumference of circle
4 x 11 = 2*pi*r
44 = 2 x (22/7) x r
r = 44 x 7 / 44 = 7 cm
Area = pi x 7^2 = (22/7) x 49 = **154 sq cm**

### Problem 15
**The height and radius of a frustum are 12 cm, with top radius 3 cm and bottom radius 9 cm. Find the volume.**

**Solution:**
V = (pi x h / 3) x (R^2 + r^2 + Rr)
= (pi x 12 / 3) x (81 + 9 + 27)
= 4*pi x 117
= **468*pi cu cm** (approximately 1470.3 cu cm)

---

## Key Formulas Quick Sheet

### 2D Shapes

| Shape | Area | Perimeter |
|-------|------|-----------|
| Triangle (general) | (1/2)bh or Heron's | a+b+c |
| Equilateral triangle | (sqrt(3)/4)a^2 | 3a |
| Circle | pi*r^2 | 2*pi*r |
| Sector | (theta/360)*pi*r^2 | 2r + arc |
| Parallelogram | bh | 2(a+b) |
| Rectangle | lb | 2(l+b) |
| Square | a^2 or d^2/2 | 4a |
| Rhombus | (1/2)d1*d2 | 4a |
| Trapezium | (1/2)(a+b)h | sum of all sides |

### 3D Shapes

| Shape | Volume | TSA |
|-------|--------|-----|
| Cube | a^3 | 6a^2 |
| Cuboid | lbh | 2(lb+bh+hl) |
| Cylinder | pi*r^2*h | 2*pi*r(r+h) |
| Cone | (1/3)*pi*r^2*h | pi*r(r+l) |
| Sphere | (4/3)*pi*r^3 | 4*pi*r^2 |
| Hemisphere | (2/3)*pi*r^3 | 3*pi*r^2 |
| Frustum | (pi*h/3)(R^2+r^2+Rr) | pi(R+r)l + pi*R^2 + pi*r^2 |

### Triangle Centers

| Center | Formed by | Key property |
|--------|-----------|-------------|
| Centroid | Medians | Divides median 2:1 from vertex |
| Incentre | Angle bisectors | r = Area/s |
| Circumcentre | Perpendicular bisectors | R = abc/(4*Area) |
| Orthocentre | Altitudes | At right-angle vertex in right triangle |

### Coordinate Geometry

| Concept | Formula |
|---------|---------|
| Distance | sqrt[(x2-x1)^2 + (y2-y1)^2] |
| Midpoint | ((x1+x2)/2, (y1+y2)/2) |
| Section formula | (mx2+nx1)/(m+n), (my2+ny1)/(m+n) |
| Slope | (y2-y1)/(x2-x1) |
| Point-to-line distance | \|ax1+by1+c\| / sqrt(a^2+b^2) |
| Triangle area (coords) | (1/2)\|x1(y2-y3)+x2(y3-y1)+x3(y1-y2)\| |

---

## CAT Strategy for Geometry

1. **Draw diagrams** - ALWAYS. Even rough diagrams help visualize the problem.
2. **Know your Pythagorean triplets** cold - recognizing them saves enormous time.
3. **Inradius and circumradius** formulas are asked directly and appear within harder problems.
4. **Similar triangles** are the most powerful geometry tool. Look for parallel lines and equal angles.
5. **For mensuration**, volume conservation is the key concept. "Melting and recasting" = volumes equal.
6. **Coordinate geometry** problems on CAT are usually straightforward formula application.
7. **Cyclic quadrilateral** properties (opposite angles = 180) appear in harder geometry questions.
8. **Right triangle shortcut for inradius**: r = (a + b - hypotenuse)/2. Saves computation.
9. **For 3D problems**, draw the cross-section. Most 3D problems reduce to 2D with the right view.
