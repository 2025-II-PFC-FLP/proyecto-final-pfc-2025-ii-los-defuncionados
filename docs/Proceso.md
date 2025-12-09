# Algoritmo de programación funciones de costo

<!-- Aqui toca hacer una introduccion general y los temas antes -->
______________________________________________________________
### 9.1 Proceso de la función `tIR`

La función `tIR` calcula el tiempo de inicio de riego de cada tablón siguiendo el orden indicado por la programación `pi`.  
Este proceso se implementa mediante **recursión de cola**, acumulando el tiempo total transcurrido hasta cada turno.

---

#### Descripción operacional

`tIR(f, pi)` funciona de la siguiente forma:

1. Se inicia con tiempoActual = 0.
2. En cada turno `idx`:
  - Se identifica el tablón actual: `tablon = pi(idx)`.
  - Se asigna `t(tablon) = tiempoActual`.
  - Se suma el tiempo de riego del tablón (`treg(f, tablon)`) al tiempo actual.
3. La recursión continúa mientras queden turnos por procesar.
4. Cuando `idx` alcanza la longitud de `pi`, se devuelve el vector final.

---

#### Ejemplo de ejecución

Considere:

```text
F = Vector(
  (5,2,3),   // Tablón 0
  (4,1,1),   // Tablón 1
  (6,3,2)    // Tablón 2
)

pi = Vector(0,2,1)
```

Evolución del proceso:

| Iteración | idx | tablón | tiempoActual | nuevoTiempo | vectorTiempos |
|-----------|-----|--------|--------------|-------------|----------------|
| 0         | 0   | 0      | 0            | 2           | [0,0,0]        |
| 1         | 1   | 2      | 2            | 5           | [0,0,2]        |
| 2         | 2   | 1      | 5            | 6           | [0,5,2]        |

Resultado final:

```scala
tIR(F, pi) = [0, 5, 2]
```

---

#### Pila de llamadas (diagrama)

```mermaid
sequenceDiagram
    autonumber
    participant Main
    participant tIR
    participant rec

    Main->>tIR: tIR(f, pi)
    tIR->>rec: rec(idx=0, tiempo=0, vector=[0,0,0])
    rec->>rec: rec(idx=1, tiempo=2, vector=[0,0,0])
    rec->>rec: rec(idx=2, tiempo=5, vector=[0,0,2])
    rec-->>tIR: [0,5,2]
    tIR-->>Main: [0,5,2]
```
---

### 9.2 Proceso de la función `costoRiegoTablon`

La función `costoRiegoTablon` calcula el costo individual de un tablón según:
- su tiempo de supervivencia,
- su tiempo de riego,
- su prioridad,
- y el tiempo en que realmente empieza a regarse (obtenido desde `tIR`).

Este proceso involucra una llamada interna importante: **tIR**, la cual es recursiva. Por lo tanto, el proceso total de `costoRiegoTablon` incluye tanto su propia lógica como la ejecución interna de `tIR`.

---

#### Entradas del ejemplo

Usamos la misma finca y programación que en la sección anterior (para mantener coherencia):

```text
F = Vector(
  (5,2,3),   // Tablón 0
  (4,1,1),   // Tablón 1
  (6,3,2)    // Tablón 2
)

pi = Vector(0, 2, 1)

```
---

### 9.3 Proceso de la función `costoRiegoFinca`

La función `costoRiegoFinca` calcula el costo total de riego de una finca sumando el costo individual de cada tablón.  
Su proceso consiste en:

1. Recorrer todos los índices de la finca.
2. Llamar a `costoRiegoTablon(i, f, pi)` para cada `i`.
3. Sumar todos los resultados.

Debido a que cada llamada a `costoRiegoTablon` invoca internamente a `tIR`, el proceso total incluye múltiples ejecuciones del proceso recursivo descrito antes.

---

#### Entradas del ejemplo

Usamos la misma finca y programación:

```text
F = Vector(
  (5,2,3),   // Tablón 0
  (4,1,1),   // Tablón 1
  (6,3,2)    // Tablón 2
)

pi = Vector(0, 2, 1)
```

---

### 9.4 Proceso de la función `costoMovilidad`

La función `costoMovilidad` calcula la suma de las distancias entre tablones consecutivos según la programación de riego `pi`.  
A diferencia de `tIR` o `costoRiegoTablon`, esta función **no contiene recursión**, sino un recorrido secuencial por pares consecutivos.

---

#### Entradas del ejemplo

Usamos la misma programación para coherencia:

```text
pi = Vector(0, 2, 1)
```


# Algoritmo de programación optima en sistemas de riego
<!-- Aqui toca hacer una introduccion general y los temas antes -->
______________________________________________________________
### 1. Permutaciones: generación de todas las rutas

### Definicion de algoritmo
```Scala
def permutaciones(tablas: Vector[Int]): Vector[ProgRiego] = {
if (tablas.isEmpty) Vector(Vector())
else {
for {
i <- tablas.indices.toVector
elem = tablas(i)
resto = tablas.patch(i, Nil, 1)
perm <- permutaciones(resto)
} yield elem +: perm
}
}
```
Explicación del proceso

Si el vector está vacío - solo existe una permutación: Vector().

En caso contrario:

- Se recorre cada índice
- Se toma un tablón como fijo
- Se generan las permutaciones del resto 
- Se pone el elemento fijo a cada permutación generada

Este diseño sigue la definición matemática recursiva estándar de permutaciones

### 2. Generación de programaciones

```Scala
def generarProgramaciones(f: Finca): Vector[ProgRiego] =
permutaciones((0 until f.length).toVector)
```

- Representa la finca como índices [0, 1, ..., n−1].

- Luego obtiene todas sus permutaciones.

### 3. Validaciones

#### 3.1 No hay tablones repetidos
```Scala
def noRepiteTablones(pi: ProgRiego): Boolean =
pi.distinct.length == pi.length
```
Garantiza unidad mediante operaciones de conjuntos

#### 3.2 Se incluyen todos los tablones
```Scala
def contieneTodos(f: Finca, pi: ProgRiego): Boolean =
pi.toSet == (0 until f.length).toSet
```
Verifica igualdad entre conjuntos


#### 4. Cálculo de costos
```Scala
def costoTotal(f: Finca, pi: ProgRiego, d: Distancia): Int = {
costoRiegoFinca(f, pi) + costoMovilidad(f, pi, d) }
```
Separar los costos cumple el principio funcional de composición de funciones puras.

Formula formal:

$$
\mathrm{CostoTotal}(\pi)
= \sum_{i \in \pi} f.\mathrm{tIR}(i)
\;+\;
\sum_{(a,b) \in \mathrm{pares}(\pi)} d(a,b)
$$

#### 5. Programación optima
```Scala
val todas = generarProgramaciones(f)
todas.map(pi => (pi, costoTotal(f, pi, d))).minBy(_._2)
```

Proceso:

- Genera todas las rutas posibles
- Calcula costoTotal para cada ruta
- Selecciona la de mínimo costo

#### 6. Integración de conceptos vistos en clase
- Recursión 
- Funciones puras
- Funcional: uso de map, distinct, flatMap, minBy
- Declaratividad
- Búsqueda en espacios discretos 
- Composición funcional

#### 7. Ejemplo con datos reales
Finca:

```Scala
Finca(Vector(3,1,4))
```
T = [0,1,2]
Perm(T) → 6 rutas posibles.

Ejemplo de cálculo:

π = [0,2,1]

Riego = 3 + 4 + 1 = 8

Movilidad = |0-2| + |2-1| = 2 + 1 = 3

CostoTotal = 11

<!-- ahí dejé el resto del ejemplo, toca borrarlo -->
________________________________________________________________________________
# Informe de proceso Algoritmo Factorial con Recursión de Cola

## Definición del Algoritmo

```Scala
def factorial(n: Int): BigInt = {
  @annotation.tailrec
  def loop(x: Int, acumulador: BigInt): BigInt = {
    if (x <= 1) acumulador
    else loop(x - 1, acumulador * x)
  }
  loop(n, 1)
}
```

- La función `factorial` calcula el factorial de un número `n` utilizando **recursión de cola**.
- La función interna `loop` es la que hace la recursión:
  - Recibe dos parámetros:
    - `x`: el valor actual decreciente hasta llegar a 1.
    - `acumulador`: donde se guarda el resultado parcial en cada paso.

- El decorador `@annotation.tailrec` obliga a que la función sea optimizada como recursión de cola, es decir, **no se acumulan llamados en la pila**.

## Explicación paso a paso

### Caso base

```Scala
if (x <= 1) acumulador
```

Cuando `x` llega a `1`, la función retorna directamente el valor acumulado, evitando más llamadas.

### Caso recursivo

```Scala
loop(x - 1, acumulador * x)
```

En cada llamada:

- Se reduce el valor de `x` en 1.
- Se multiplica el acumulador por `x` y se pasa a la siguiente iteración.
- Como es recursión de cola, la llamada recursiva es la **última instrucción** en ejecutarse, lo que permite a Scala optimizar la pila.

---

## Llamados de pila en recursión de cola

Ejemplo:

```Scala
factorial(5)
```

### Paso 1: Llamada inicial

```Scala
loop(5, 1)
```

### Paso 2: Primera iteración

```Scala
loop(4, 5)   // acumulador = 1 * 5
```

### Paso 3: Segunda iteración

```Scala
loop(3, 20)  // acumulador = 5 * 4
```

### Paso 4: Tercera iteración

```Scala
loop(2, 60)  // acumulador = 20 * 3
```

### Paso 5: Cuarta iteración

```Scala
loop(1, 120) // acumulador = 60 * 2
```

### Paso 6: Caso base

```Scala
return 120
```

---

## Diferencia con recursión normal

- En **recursión normal** cada llamada queda en la pila esperando a que termine la siguiente, lo que puede causar desbordamiento si `n` es muy grande.
- En **recursión de cola**, el compilador transforma el proceso en un **bucle optimizado**, por lo que no se guarda cada llamada en la pila y el algoritmo puede ejecutarse para valores muy grandes sin problema.

---

## Ejemplo de uso

```Scala
val resultado = factorial(5)
println(resultado)  // 120
```

El resultado de `factorial(5)` es `120`.

## Diagrama de llamados de pila con recursión de cola

```mermaid
sequenceDiagram
    participant Main as factorial(5)
    participant L1 as loop(5, 1)
    participant L2 as loop(4, 5)
    participant L3 as loop(3, 20)
    participant L4 as loop(2, 60)
    participant L5 as loop(1, 120)

    Main->>L1: llamada inicial
    L1->>L2: tail call con (4, 5)
    L2->>L3: tail call con (3, 20)
    L3->>L4: tail call con (2, 60)
    L4->>L5: tail call con (1, 120)
    L5-->>Main: return 120
```
