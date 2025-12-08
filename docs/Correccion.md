**Fundamentos de Programación Funcional y Concurrente**
# Argumentación de corrección de los algoritmos implementados

En esta sección se argumenta que los algoritmos implementados en riego.scala son correctos con respecto a su especificación.
La corrección se demuestra usando inducción estructural, notación matemática y llamados explícitos de los algoritmos.

________________________________________________________________________________________
### 1. Corrección de las funciones de generación aleatoria (fincaAlAzar)
```scala
def fincaAlAzar(long: Int): Finca = // Aqui implementamos el generar numero de tablones. (el parametro long define el numero de tablones)
    Vector.fill(long)(
      (random.nextInt(long * 2) + 1,
        random.nextInt(long) + 1, // Aqui implementamos el generar duracion de riego (Este valor es el tiempo del regado)
        random.nextInt(4) + 1)
    )
```
Con esto generamos una finca $F$ de $n$ tablones donde: $ F = \langle T_0, T_1, \ldots, T_{n-1} \rangle$ $$[
\forall i \in \{0, \ldots, n-1\}:
\quad
T_i = \langle ts_i^F, \; tr_i^F, \; p_i^F \rangle
] $$ con las restricciones: $$[
1 \le ts_i^F \le 2n,
\qquad
1 \le tr_i^F \le n,
\qquad
1 \le p_i^F \le 4
]$$  
**Demostracion de correccion:** La funcion `Vector.fill(long)(expresión)` genera un vector de tamaño `long` evaluando `expresion` en cada posición. 

Para cada tablon Ti: 
* random.nextInt(long * 2) + 1 Genera valores en [1,2n] cumpliendo $1 \le ts_i^F \le 2n.$
* $$ \texttt{random.nextInt(long) + 1}
  \quad \text{genera valores en } [1, n],
  \quad \text{cumpliendo } 1 \le tr_i^F \le n.
   $$
* $$ \texttt{random.nextInt(4) + 1}
  \quad \text{genera valores en } [1, 4],
  \quad \text{cumpliendo } 1 \le p_i^F \le 4.
  $$
* Para construcción, `Vector.fill` garantiza $|F| = n.$
**Conclusión:** `fincaAlAzar` genera fincas que cumplen todas las restricciones especificadas.

### 2. Correccion de la función distanciaAlAzar
```scala 
def distanciaAlAzar(long: Int): Distancia = { // Aqui implementamos el generar distancias
  val v = Vector.fill(long, long)(random.nextInt(long * 3) + 1)
  Vector.tabulate(long, long)((i, j) =>
    if (i == j) 0
    else if (i < j) v(i)(j)
    else v(j)(i)
  )
}
```
Con esto generamos una matriz de distancia $Df$ de tamaño $n x n $ que cumpla:
* **Simetria:** $D_F[i,j] = D_F[j,i], \qquad \forall\, i,j.$
* **Diagonal cero:** $D_F[i,i] = 0, \qquad \forall\, i.$
* **No negatividad:** $D_F[i,j] \ge 0, \qquad \forall\, i,j.$

**Demostracion de correccion:** La función construye $Df$ mediante `Vector.tabulate` que aplica una función a cada par (i,j).
**Propiedad 1: Diagonal cero:** $Si ( i = j ), entonces: D_F[i,j] = 0.$ Esto se garantiza por la condicion ${if (i == j) = 0}.$
**Propiedad 2: Simetria:** $$Para ( i \neq j ), la función asigna:
\
D_F[i,j] =
\begin{cases}
v[i][j], & \text{si } i < j, \\
v[j][i], & \text{si } i > j.
\end{cases}
\
$$
Luego $D_F[i,j] = D_F[j,i]$13.Propiedad 3 - No negatividad:La matriz base $v$ se genera con random.nextInt(long * 3) + 1, produciendo valores en $[1, 3n]$. Como $D_F[i,j] \in \{0\} \cup v[i][j]$, se cumple $D_F[i,j] \geq 0, \forall i,j$.Conclusión: distanciaAlAzar genera matrices simétricas con diagonal cero y valores no negativos.

### 3. Correccion de las funciones de exploracion
```scala 
def tsup(f: Finca, i: Int): Int = f(i)._1
def treg(f: Finca, i: Int): Int = f(i)._2
def prio(f: Finca, i: Int): Int = f(i)._3
```
**Especificación formal:**
Dado $F = \langle T_0, \ldots, T_{n-1} \rangle$ donde $T_i = \langle ts^F_i, tr^F_i, p^F_i \rangle$: $$\text{tsup}(F, i) = ts^F_i$$ $$\text{treg}(F, i) = tr^F_i$$ $$\text{prio}(F, i) = p^F_i$$
**Demostracion de correccion:** En Scala, una tupla (a, b, c) permite acceso mediante . _1, . _2, . _3. 
* $f(i) retorna T_i = (ts^F_i, tr^F_i, p^F_i)$.
* $f(i)._1 retorna el primer componente: ts^F_i$
* $f(i)._2 retorna el segundo componente: tr^F_i$
* $f(i)._3 retorna el tercer componente: p^F_i$

Conclusión: Los getters son correctos por definición de acceso a tuplas.

### 3. Corrección de la función tIR
```scala 
def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego
```
Especificación matemática:
Dado $\Pi = \langle \pi_0, \pi_1, \ldots, \pi_{n-1} \rangle$, calcular $t^\Pi_i$ para cada tablón $i$:
$$t^\Pi_{\pi_0} = 0$$ $$t^\Pi_{\pi_j} = t^\Pi_{\pi_{j-1}} + tr^F_{\pi_{j-1}}, \quad j = 1, \ldots, n-1$$

**Implementación:** 
```scala 
def tIR(f: Finca, pi: ProgRiego): TiempoInicioRiego = {
  @tailrec
  def rec(idx: Int, tiempoActual: Int, resultado: TiempoInicioRiego): TiempoInicioRiego =
    if (idx >= pi.length) resultado
    else {
      val tablonActual = pi(idx)
      val nuevoResultado = resultado.updated(tablonActual, tiempoActual)
      val nuevoTiempo = tiempoActual + treg(f, tablonActual)
      rec(idx + 1, nuevoTiempo, nuevoResultado)
    }

  rec(0, 0, Vector.fill(f.length)(0))
}
```
Demostración por inducción iterativa

Invariante de iteración:$$\text{Inv}(k, t_k, r_k) \equiv 0 \leq k \leq n \land t_k = \sum_{j=0}^{k-1} tr^F_{\pi_j} \land \forall j < k: r_k[\pi_j] = t^\Pi_{\pi_j}$$

Caso base ($k=0$):
* $t_0 = 0 = \sum_{j=0}^{-1} tr^F_{\pi_j}$ (suma vacía)23.
* $\forall j < 0: r_0[\pi_j] = t^\Pi_{\pi_j}$ (vacuamente verdadero)24.

El invariante se cumple

**Caso inductivo (Inv($k$) $\Rightarrow$ Inv($k+1$)):** La transformación es $s_k=(k,t_k,r_k) \rightarrow s_{k+1}=(k+1,t_k+tr^F_{\pi_k},r_k[\pi_k:=t_k])$.
1. Cálculo de $t_{k+1}$:
   $$t_{k+1} = t_k + tr^F_{\pi_k} = \sum_{j=0}^{k-1} tr^F_{\pi_j} + tr^F_{\pi_k} = \sum_{j=0}^{k} tr^F_{\pi_j}$$
2. Cálculo de $r_{k+1}$:
   Por hipótesis inductiva, $t_k = t^\Pi_{\pi_k}$. El programa actualiza $r_{k+1}[\pi_k] = t_k = t^\Pi_{\pi_k}$.
   Además, $\forall j < k: r_{k+1}[\pi_j] = r_k[\pi_j] = t^\Pi_{\pi_j}$ (no se modifican posiciones previas).
   Por lo tanto: $\forall j \leq k: r_{k+1}[\pi_j] = t^\Pi_{\pi_j}$.

**El invariante se preserva**

**Terminación:** uando $\text{idx}=n$, el invariante garantiza $r_n[\pi_j] = t^\Pi_{\pi_j}$ para todo $j < n$.
Como $\{\pi_0, \ldots, \pi_{n-1}\} = \{0, \ldots, n-1\}$, se tiene $\forall i \in \{0, \ldots, n-1\}: r_n[i] = t^\Pi_i$.

**Conclusión:** tIR es correcta por inducción sobre la iteración.

### 4. Correción de las funciones de validacion (Finca)
```scala 
def validarFinca(f: Finca): Boolean =
  f.forall { case (ts, tr, p) =>
    ts > 0 && tr > 0 && p > 0 && p <= 4
  }
```
Verificar que $\forall i \in \{0, \ldots, n-1\}$:
$$ts^F_i > 0 \land tr^F_i > 0 \land 1 \leq p^F_i \leq 4$$

**Conclusión:** validarFinca es correcta por definición del cuantificador universal.
### 5.cCorreción de las funciones de validacion (Distancias)
```scala 
def validarDistancias(d: Distancia): Boolean =
  d.indices.forall(i =>
    d(i).length == d.length &&
    d(i)(i) == 0 &&
    d.indices.forall(j => d(i)(j) == d(j)(i) && d(i)(j) >= 0)
  )
```
Verificar que $D_F$ cumple:
* Matriz cuadrada: $|D_F[i]| [cite_start]= n, \forall i$.
* Diagonal cero: $D_F[i,i] = 0, \forall i$.
* Simetría: $D_F[i,j] = D_F[j,i], \forall i,j$.
* No negatividad: $D_F[i,j] \geq 0, \forall i,j$.

**Conclusión:** `validarDistancias` es correcta por verificación exhaustiva de propiedades matriciales. 
### 6. Correción de las funciones de validacion (ProgRiego)
```scala 
def validarProgRiego(pi: ProgRiego, n: Int): Boolean =
  pi.length == n && pi.toSet == (0 until n).toSet
```
Verificar que $\Pi$ es una permutación válida de $\{0, 1, \ldots, n-1\}$ $$|\Pi| = n \land \text{Set}(\Pi) = \{0, \ldots, n-1\}$$

**Conclusión:** `validarProgRiego` es correcta por propiedades de conjuntos y cardinalidad.

### 7. Corrección de funciones auxiliares con `Random` parametrizado
`fincaAlAzarconRandom` y `distanciaAlAzarconRandom` son funciones identicas a sus contrapartes sin parámetro `Random`, excepto que usan un generador r en lugar de un random.

**Propiedad de corrección por equivalencia:**
Si $f_1$ y $f_2$ difieren solo en la fuente de aleatoriedad pero mantienen la misma lógica de construcción, entonces $\text{correcto}(f_1) \Rightarrow \text{correcto}(f_2)$.

**Conclusión:** `fincaAlAzarconRandom` y `distanciaAlAzarconRandom` son correctas por equivalencia estructural.

**Conclusión general:** Se ha demostrado formalmente la corrección de todos los algoritmos implementados en esta primera parte:
* **Funciones de generación aleatoria:** (`fincaAlAzar`, `distanciaAlAzar`): Correctas por construcción y cumplimiento de restricciones especificadas.
* **Funciones de exploración:** (`tsup`, `treg`, `prio`): Correctas por definición de acceso a componentes de tuplas.
* **Función tIR:** Correcta por inducción sobre iteración con invariante bien definido.
* **Funciones de validación:** 
   * `validarFinca`: Correcta por cuantificación universal.
   * `validarDistancias` : Correcta por verificación exhaustiva de propiedades matriciales.
   * `validarProgRiego` : Correcta por teoría de conjuntos
* **Versiones con random parametrizado:** Correctas por equivalencia estructural. 

### 1. Corrección de la función permutaciones

def permutaciones(tablas: Vector[Int]): Vector[ProgRiego]

Debe generar todas las permutaciones posibles del vector de tablones.
Formalmente, queremos demostrar:    

$$
\forall T \in \text{List}[\text{Int}] :\ \text{permutaciones}(T) = \mathrm{Perm}(T)
$$

donde Perm(t) es el conjunto matemático de todas las permutaciones de t


#### 1.1 Caso base 
Si el vector es vacío:
if (tablas.isEmpty) Vector(Vector())

Entonces:
$$\mathrm{Perm}([]) = \{ [] \}$$

Esto coincide con la definición matemática: el conjunto de permutaciones de la lista vacía es una lista vacía única

#### 1.2 Caso inductivo 
Supongamos que la función es correcta para listas de tamaño k:

permutaciones(Tk)=Perm(Tk)

Queremos demostrar que es correcta para una lista de tamaño k+1:

```scala
for {
i <- tablas.indices
elem = tablas(i)
resto = tablas.patch(i, Nil, 1)
perm <- permutaciones(resto)
} yield elem +: perm
```

Matemáticamente, esto corresponde a:

$$
\mathrm{Perm}(T) = \bigcup_{x \in T} \{\, x :: P \mid P \in \mathrm{Perm}(T \setminus \{x\}) \,\}
$$

Esta es exactamente la definición recursiva de permutaciones.

- La función toma cada elemento,

- genera las permutaciones del resto (hipótesis inductiva),

- y lo agrega al inicio, obteniendo todas las combinaciones posibles.

Por lo tanto:

P(Tk+1)=Perm(Tk+1)

La función es correcta

### 2. Corrección de generarProgramaciones

def generarProgramaciones(f: Finca): Vector[ProgRiego] =
permutaciones((0 until f.length).toVector)

Esta función simplemente genera el vector:
[0,1,2,…,n−1]

y usa permutaciones para obtener todas las rutas posibles.

generarProgramaciones(f)=Perm({0,…,n−1})

### 3. Corrección de validaciones

#### 3.1 noRepiteTablones

pi.distinct.length == pi.length

Matemáticamente: 

$$
\text{noRepiteTablones}(\pi) \;\equiv\; |\text{unique}(\pi)| = |\pi|
$$

Esto es verdadero solo si no hay elementos repetidos.

#### 3.2 contieneTodos

pi.toSet == (0 until f.length).toSet

Demuestra:

Set(π)={0,…,n−1}

Es decir que la ruta incluye todos los tablones y exactamente una vez

### 4. Corrección del cálculo de costos

#### 4.1 Costo total 
costoTotal = costoRiegoFinca + costoMovilidad

$$\mathrm{CostoTotal}(\pi)
= \sum_{i \in \pi} f.\mathrm{tIR}(i)
\;+\;
\sum_{(a,b) \in \mathrm{pares}(\pi)} d(a,b)
$$

primer término = costo de riego
segundo término = costo de moverse entre tablones consecutivos

### 5. Corrección de programacionOptima

```scala
val todas = generarProgramaciones(f)
todas.map(pi => (pi, costoTotal(f, pi, d))).minBy(_._2)
```
Se quiere demostrar que selecciona la programación con menor costo:

$$
\mathrm{Opt}(f) = \arg\!\min_{\pi \in \mathrm{Perm}(T)} \mathrm{CostoTotal}(\pi)
$$

El algoritmo:

- genera todas las rutas

- calcula costoTotal para cada una 

- aplica minBy (devuelve la pareja con el costo mínimo)

Esto cumple: 

$$
(\pi_{\text{opt}},\, c_{\text{opt}})
= \min_{\pi \in \mathrm{Perm}(T)} \mathrm{CostoTotal}(\pi)
$$


### 6. Conclusion formal
Usando inducción estructural y definiciones matemáticas, demostramos:

$$
\forall f, d:\
\mathrm{programacionOptima}(f,d)
= \arg\!\min_{\pi \in \mathrm{Perm}(f)} \mathrm{CostoTotal}(\pi)
$$

La función permutaciones es correcta por definición recursiva
- La generación de programaciones es correcta por composición
- Las validaciones son correctas por teoría de conjuntos
- El cálculo de costos corresponde exactamente a la especificación
- La selección de la mejor ruta es correcta usando búsqueda exhaustiva

### 7. Llamados explícitos como parte de la demostración
```scala
Finca(Vector(3,1,4))
```

T=[0,1,2]

Perm(T)=[0,1,2],[0,2,1],[1,0,2],[1,2,0],[2,0,1],[2,1,0]

Para cada ruta se calcula:

CostoTotal(pi)

Ejemplo: 

π=[0,2,1]

Riego=3+4+1=8

Movilidad=∣0−2∣+∣2−1∣=2+1=3

CostoTotal=11

El algoritmo revisa todas las rutas y escoge la menor

________________________________________________________________________
# Ejemplo informe de corrección

**Fundamentos de Programación Funcional y Concurrente**  
Documento realizado por el docente Juan Francisco Díaz.

---


## Argumentación de corrección de programas



### Argumentando sobre corrección de programas recursivos

Sea $f : A \to B$ una función, y $A$ un conjunto definido recursivamente (recordar definición de matemáticas discretas I), como por ejemplo los naturales o las listas.

Sea $P_f$ un programa recursivo (lineal o en árbol) desarrollado en Scala (o en cualquier lenguaje de programación) hecho para calcular $f$:

```scala
def Pf(a: A): B = { // Pf recibe a de tipo A, y devuelve f(a) de tipo B
  ...
}
```

¿Cómo argumentar que \$P_f(a)\$ siempre devuelve \$f(a)\$ como respuesta? Es decir, ¿cómo argumentar que \$P_f\$ es correcto con respecto a su especificación?

La respuesta es sencilla, demostrando el siguiente teorema:

$$
\forall a \in A : P_f(a) == f(a)
$$

Cuando uno tiene que demostrar que algo se cumple para todos los elementos de un conjunto definido recursivamente, es natural usar **inducción estructural**.

En términos prácticos, esto significa demostrar que:

- Para cada valor básico \$a\$ de \$A\$, se tiene que \$P_f(a) == f(a)\$.
- Para cada valor \$a \in A\$ construido recursivamente a partir de otro(s) valor(es) \$a' \in A\$, se tiene que \$P_f(a') == f(a') \rightarrow P_f(a) == f(a)\$ (hipótesis de inducción).

---

#### Ejemplo: Factorial Recursivo

Sea \$f : \mathbb{N} \to \mathbb{N}\$ la función que calcula el factorial de un número natural, \$f(n) = n!\$.

Programa en Scala:

```scala
def Pf(n: Int): Int = {
  if (n == 0) 1 else n * Pf(n - 1)
}
```

Queremos demostrar que:

$$
\forall n \in \mathbb{N} : P_f(n) == n!
$$

- **Caso base**: \$n = 0\$

$$
P_f(0) \to 1 \quad \land \quad f(0) = 0! = 1
$$

Entonces \$P_f(0) == f(0)\$.

- **Caso inductivo**: \$n = k+1\$, \$k \geq 0\$.

$$
P_f(k+1) \to (k+1) \cdot P_f(k)
$$

Usando la hipótesis de inducción:

$$
\to (k+1) \cdot k! = (k+1)!
$$

Por lo tanto, \$P_f(k+1) == f(k+1)\$.

**Conclusión**: \$\forall n \in \mathbb{N} : P_f(n) == n!\$

---

#### Ejemplo: El máximo de una lista

Sea \$f : \text{List}\[\mathbb{N}] \to \mathbb{N}\$ la función que calcula el máximo de una lista no vacía.

Programa en Scala:

```scala
def maxLin(l: List[Int]): Int = {
  if (l.tail.isEmpty) l.head
  else math.max(maxLin(l.tail), l.head)
}
```

Queremos demostrar que:

$$
\forall n \in \mathbb{N} \setminus \{0\} :
P_f(\text{List}(a_1, \ldots, a_n)) == f(\text{List}(a_1, \ldots, a_n))
$$

- **Caso base**: \$n=1\$.

$$
P_f(\text{List}(a_1)) \to a_1 \quad \land \quad f(\text{List}(a_1)) = a_1
$$

- **Caso inductivo**: \$n=k+1\$.

$$
P_f(L) \to \text{math.max}(P_f(\text{List}(a_2, \ldots, a_{k+1})), a_1)
$$

Dependiendo del mayor entre \$a_1\$ y \$b\$ (el máximo del resto de la lista), se cumple que \$P_f(L) == f(L)\$.

**Conclusión**:

$$
\forall n \in \mathbb{N} \setminus \{0\} : P_f(\text{List}(a_1, \ldots, a_n)) == f(\text{List}(a_1, \ldots, a_n))
$$

---

### Argumentando sobre corrección de programas iterativos

Para argumentar la corrección de programas iterativos, se debe formalizar cómo es la iteración:

- Representación de un estado \$s\$.
- Estado inicial \$s_0\$.
- Estado final \$s_f\$.
- Invariante de la iteración \$\text{Inv}(s)\$.
- Transformación de estados \$\text{transformar}(s)\$.

Programa iterativo genérico:

```scala
def Pf(a: A): B = {
  def Pf_iter(s: Estado): B =
    if (esFinal(s)) respuesta(s) else Pf_iter(transformar(s))
  Pf_iter(s0)
}
```

---

#### Ejemplo: Factorial Iterativo

```scala
def Pf(n: Int): Int = {
  def Pf_iter(i: Int, n: Int, ac: Int): Int =
    if (i > n) ac else Pf_iter(i + 1, n, i * ac)
  Pf_iter(1, n, 1)
}
```

- Estado \$s = (i, n, ac)\$
- Estado inicial \$s_0 = (1, n, 1)\$
- Estado final: \$i = n+1\$
- Invariante: \$\text{Inv}(i,n,ac) \equiv i \leq n+1 \land ac = (i-1)!\$
- Transformación: \$(i, n, ac) \to (i+1, n, i \cdot ac)\$

Por inducción sobre la iteración, se demuestra que al llegar a \$s_f\$, \$ac = n!\$.

---

#### Ejemplo: El máximo de una lista

```scala
def maxIt(l: List[Int]): Int = {
  def maxAux(max: Int, l: List[Int]): Int = {
    if (l.isEmpty) max
    else maxAux(math.max(max, l.head), l.tail)
  }
  maxAux(l.head, l.tail)
}
```

- Estado \$s = (max, l)\$
- Estado inicial \$s_0 = (a_1, \text{List}(a_2, \ldots, a_k))\$
- Estado final: \$l = \text{List}()\$
- Invariante: \$\text{Inv}(max, l) \equiv max = f(\text{prefijo})\$
- Transformación: \$(max, l) \to (\text{math.max}(max, l.head), l.tail)\$

Por inducción, al llegar al estado final, \$max = f(L)\$.

**Conclusión**:

$$
P_f(L) == f(L)
$$_
