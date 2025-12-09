# Informe de Paralelización

## 1. Objetivo

El objetivo de esta sección es analizar el impacto de la paralelización sobre los tiempos de ejecución del problema del riego óptimo [2]. Se comparan las versiones:

*   **Secuencial:** implementadas en el objeto `Riego` [2].
*   **Paralela:** implementadas en el objeto `RiegoParalelo` [2].

Se evalúan los siguientes puntos [2]:

1.  El cálculo de los costos de riego (`costoRiegoFinca`) y de movilidad (`costoMovilidad`).
2.  La generación de todas las programaciones posibles (búsqueda factorial: `generarProgramacionesRiego`).
3.  El cálculo de la programación óptima (búsqueda exhaustiva: `ProgramacionRiegoOptimo`).

Para cada caso se miden los tiempos de ejecución, se calcula la aceleración obtenida y se discuten los resultados a la luz de la ley de Amdahl y de la sobrecarga asociada al paralelismo [3].

## 2. Estrategia de Paralelización

La estrategia principal utilizada fue el **Paralelismo de Datos** mediante la implementación de colecciones paralelas de Scala (`.par`) [4].

Se mantuvo la misma semántica de las funciones secuenciales: la versión paralela debe producir exactamente los mismos resultados que la secuencial, lo cual se verificó mediante pruebas automatizadas [3].

### 2.1. Costos de Riego y Movilidad

Las versiones paralelas `costoRiegoFincaPar(f, pi)` y `costoMovilidadPar(f, pi, d)` paralelizan los recorridos de colecciones (`.par`) usando operaciones funcionales como `map` y `sum` [4]. La estrategia es dividir el cálculo del costo entre varios hilos y combinar los resultados mediante una reducción (`sum`) [4].

El resultado numérico debe ser idéntico en ambas versiones [5].

### 2.2. Generación de Programaciones de Riego

La función `generarProgramacionesRiegoPar(f)` aplica el mismo algoritmo recursivo que `generarProgramaciones(f)`, pero distribuye el trabajo entre varios hilos, fijando prefijos de la permutación y generando en paralelo las permutaciones de los sufijos [5], [6].

Debido al crecimiento factorial $O(n!)$, solo se evaluaron tamaños hasta $n=10$, ya que para valores mayores la cantidad de permutaciones se vuelve inmanejable en tiempo razonable [6].

### 2.3. Programación Óptima

La función `ProgramacionRiegoOptimoPar(f, d)` mantiene la lógica de generar todas las permutaciones y calcular el costo total $C(\Pi) = CR_{\Pi}^F + CM_{\Pi}^F$ [7], pero reparte el cálculo del costo entre varios hilos, evaluando en paralelo subconjuntos de permutaciones y combinando los mínimos parciales [7].

Dado que la complejidad es $n!$, se evaluaron tamaños [8]:
*   **Pequeña:** $n=6$ (720 permutaciones)
*   **Mediana:** $n=7$ (5040 permutaciones)
*   **Grande:** $n=8$ (40 320 permutaciones)

## 3. Configuración Experimental

*   **Lenguaje:** Scala (versión del proyecto) [8].
*   **Biblioteca de benchmarking:** `org.scalameter` [8].
*   **Generación de datos:** Se usaron las funciones `fincaAlAzarconRandom` y `distanciaAlAzarconRandom` del objeto `Riego` para asegurar entradas aleatorias y repetibles [8].
*   **Ejecución de benchmarks:** Se utilizó el objeto `RiegoBenchmarking`, que compara el tiempo secuencial y paralelo de cada operación [9].
*   **Métrica:** Tiempo promedio en milisegundos (ms) [9].

La aceleración se define como [9]:

$$\text{Aceleración}(\%) = \frac{T_{\text{seq}} - T_{\text{par}}}{T_{\text{seq}}} \cdot 100$$

Donde $T_{\text{seq}}$ es el tiempo de la versión secuencial y $T_{\text{par}}$ el tiempo de la versión paralela. El *speedup* $S$ también se puede expresar como $S = \frac{T_{\text{seq}}}{T_{\text{par}}}$ [10].

## 4. Resultados de Benchmarking

### 4.1. Costos de Riego y Movilidad (Sección 3.1)

Se probaron tamaños grandes (10, 50 y 100 tablones) [10].

| Tamaño (tablones) | Secuencial (ms) | Paralelo (ms) | Aceleración (%) |
| :---------------- |----------------:|--------------:|----------------:|
| 10                |           33,37 |        106,06 |         -217,80 |
| 50                |           11,34 |         10,04 |           11,45 |
| 100               |           23,51 |         13,90 |           40,89 |

*(Nota: Estos valores deben ser rellenados con los tiempos reales obtenidos de la ejecución del `RiegoBenchmarking` para la parte de costos, según indica el enunciado)* [10].

### 4.2. Generación de Programaciones (Sección 3.2)

Datos reales obtenidos con el benchmark [11]:

| Tamaño (tablones) | Secuencial (ms) | Paralelo (ms) | Aceleración (%) |
| :---------------- | --------------: | ------------: | --------------: |
| 8                 | 32.07           | 18.05         | 43.72           |
| 10                | 3807.80         | 1440.21       | 62.18           |

**Interpretación:**
Para $n=8$, la versión paralela es aproximadamente $S \approx \frac{32.07}{18.05} \approx 1.78$ veces más rápida [11]. Para $n=10$, el *speedup* es aún mayor: $S \approx \frac{3807.80}{1440.21} \approx 2.64$ [11].

La sobrecarga de la paralelización se amortiza y se obtienen aceleraciones significativas para tamaños grandes, debido a que el volumen de trabajo (crecimiento factorial) domina la carga de cómputo [11].

### 4.3. Programación Óptima (Sección 3.3)

Datos reales obtenidos [12]:

| Tamaño (tablones) | Secuencial (ms) | Paralelo (ms) | Aceleración (%) |
| :---------------- | --------------: | ------------: | --------------: |
| 6                 | 50.16           | 79.89         | -59.28          |
| 7                 | 150.22          | 156.37        | -4.09           |
| 8                 | 1498.93         | 544.19        | 63.70           |

**Observaciones:**
Para $n=6$ y $n=7$, la aceleración es negativa [12]. Esto indica que el costo de la sobrecarga (creación de hilos, repartición y combinación de resultados) es mayor que el trabajo útil, dominando el tiempo de ejecución [12], [13].

Para $n=8$, el volumen de trabajo (40 320 permutaciones) es lo suficientemente grande y la versión paralela logra una aceleración del $63.70\%$, con un *speedup* aproximado de $S \approx \frac{1498.93}{544.19} \approx 2.75$ [14].

## 5. Análisis con la Ley de Amdahl

La ley de Amdahl establece el *speedup* máximo teórico ($S$) de un programa paralelo [14]:

$$S(p) = \frac{1}{(1-f) + \frac{f}{p}}$$

donde $p$ es el número de procesadores/hilos de ejecución y $f$ es la fracción paralelizable del programa [15].

Tomando el caso $n=8$ de la programación óptima, donde $S \approx 2.75$ [15], y suponiendo una máquina con $p=4$ núcleos, podemos estimar la fracción paralelizable $f$:

$$2.75 \approx \frac{1}{(1-f) + \frac{f}{4}}$$

Al resolver esta ecuación, se obtiene un valor aproximado de $f \approx 0.85$ [13]. Esto implica que alrededor del **$85\%$ del trabajo de la función de programación óptima puede paralelizarse de manera efectiva** [13].

Los resultados para $n=6$ y $n=7$ ilustran que, cuando el tamaño del problema es pequeño, la parte secuencial y la sobrecarga de sincronización dominan el tiempo de ejecución, haciendo que la versión paralela sea más lenta que la secuencial [13].

**En resumen** [16]:
*   **Problemas pequeños:** la sobrecarga del paralelismo domina, resultando en aceleración negativa o cercana a 0.
*   **Problemas grandes:** el costo de cómputo domina, resultando en *speedups* significativos.

## 6. Relación con los Casos de Prueba

Se implementaron pruebas automatizadas (`RiegoParaleloTest`) que verifican la corrección funcional de las versiones paralelas frente a las secuenciales para diferentes tamaños de entrada [16], [17].

Específicamente, se verificó que [16], [17]:
*   `costoRiegoFincaPar` y `costoMovilidadPar` producen el mismo resultado que sus versiones secuenciales.
*   `generarProgramacionesRiegoPar` genera el mismo conjunto de programaciones que `generarProgramaciones`.
*   `ProgramacionRiegoOptimoPar` devuelve el mismo costo óptimo que `programacionOptima`.

Estas pruebas garantizan que las diferencias observadas en los tiempos de ejecución se deben exclusivamente al paralelismo y no a errores lógicos [17].

## 7. Conclusiones

*   **Corrección funcional:** Las versiones paralelas son funcionalmente equivalentes a las versiones secuenciales, según demuestran los tests [17].
*   **Generación de programaciones ($N!$):** La paralelización es muy beneficiosa para tamaños grandes ($n=10$), logrando *speedups* superiores a 2x debido al gran volumen de trabajo [18].
*   **Programación óptima ($N!$):** El *speedup* de aproximadamente $2.75$ para $n=8$ demuestra la ventaja de la paralelización cuando el volumen de trabajo es alto [18]. No obstante, para fincas pequeñas ($n=6$ y $n=7$) se introduce sobrecarga, haciendo a la versión secuencial preferible [18].
*   **Recomendación práctica:** Es recomendable usar la versión secuencial para fincas pequeñas y activar la versión paralela únicamente a partir de un tamaño de finca donde se observe una aceleración positiva (por ejemplo, a partir de $8$ tablones en este proyecto) [19].