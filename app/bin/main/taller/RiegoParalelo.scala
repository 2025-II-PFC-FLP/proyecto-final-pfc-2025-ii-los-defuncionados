package taller

import scala.collection.parallel.CollectionConverters._
import scala.util.Random

/**
 * Implementación paralela del problema de riego.
 * Complementa el archivo Riego.scala SIN modificarlo.
 */
object RiegoParalelo {

  /* =========================================================
   * 1. COSTOS PARALELOS
   * ========================================================= */

  /**
   * Costo de riego de una finca calculado en paralelo.
   */
  def costoRiegoFincaPar(f: Riego.Finca, pi: Riego.ProgRiego): Int = {
    require(Riego.validarFinca(f), "Finca inválida")
    require(Riego.validarProgRiego(pi, f.length), "ProgRiego inválida")

    (0 until f.length).par
      .map(i => Riego.costoRiegoTablon(i, f, pi))
      .sum
  }

  /**
   * Costo de movilidad calculado en paralelo.
   */
  def costoMovilidadPar(f: Riego.Finca, pi: Riego.ProgRiego, d: Riego.Distancia): Int = {
    require(Riego.validarProgRiego(pi, f.length), "ProgRiego inválida")
    require(Riego.validarDistancias(d), "Matriz de distancias inválida")

    if (pi.length <= 1) 0
    else {
      (0 until pi.length - 1).par.map { i =>
        val a = pi(i)
        val b = pi(i + 1)
        d(a)(b)
      }.sum
    }
  }

  /**
   * Costo total paralelo (riego + movilidad).
   */
  def costoTotalPar(
                     f: Riego.Finca,
                     pi: Riego.ProgRiego,
                     d: Riego.Distancia
                   ): Int =
    costoRiegoFincaPar(f, pi) + costoMovilidadPar(f, pi, d)

  /* =========================================================
   * 2. GENERACIÓN DE PROGRAMACIONES (PARALELO CONTROLADO)
   * ========================================================= */

  /**
   * Permutaciones paralelas SOLO en el primer nivel.
   * Evita problemas de tipos y explosión de hilos.
   */
  private def permutacionesPar(tablas: Vector[Int]): Vector[Riego.ProgRiego] = {
    if (tablas.isEmpty) Vector(Vector())
    else {
      tablas.indices.toVector.par.flatMap { i =>
        val elem = tablas(i)
        val resto = tablas.patch(i, Nil, 1)
        Riego.permutaciones(resto).map(elem +: _)
      }.toVector
    }
  }

  /**
   * Genera todas las programaciones posibles en paralelo.
   */
  def generarProgramacionesRiegoPar(
                                     f: Riego.Finca
                                   ): Vector[Riego.ProgRiego] =
    permutacionesPar((0 until f.length).toVector)

  /* =========================================================
   * 3. PROGRAMACIÓN ÓPTIMA PARALELA
   * ========================================================= */

  /**
   * Calcula la programación óptima usando paralelismo.
   */
  def ProgramacionRiegoOptimoPar(
                                  f: Riego.Finca,
                                  d: Riego.Distancia
                                ): (Riego.ProgRiego, Int) = {

    // Usamos generación secuencial o paralela (ambas válidas)
    val programaciones = Riego.generarProgramaciones(f)

    programaciones.par
      .map(pi => (pi, costoTotalPar(f, pi, d)))
      .minBy(_._2)
  }

  /* =========================================================
   * 4. MAIN COMPARATIVO (SECUENCIAL vs PARALELO)
   * ========================================================= */

  def main(args: Array[String]): Unit = {
    println("\n============================================")
    println(" COMPARACIÓN: SOLUCIÓN SECUENCIAL vs PARALELA")
    println("============================================")

    val seed = 123
    val r = new Random(seed)

    val n = 8 // ⚠ factorial, no subir demasiado

    val finca = Riego.fincaAlAzarconRandom(r, n)
    val distancias = Riego.distanciaAlAzarconRandom(r, n)

    // ------------------- SECUENCIAL -------------------
    val t1 = System.nanoTime()
    val (piSec, costoSec) =
      Riego.programacionOptima(finca, distancias)
    val t2 = System.nanoTime()
    val tiempoSec = (t2 - t1) / 1e6

    // ------------------- PARALELO --------------------
    val t3 = System.nanoTime()
    val (piPar, costoPar) =
      ProgramacionRiegoOptimoPar(finca, distancias)
    val t4 = System.nanoTime()
    val tiempoPar = (t4 - t3) / 1e6

    // ------------------- RESULTADOS ------------------
    println(f"\nSecuencial -> costo: $costoSec | tiempo: $tiempoSec%.2f ms")
    println(f"Paralelo   -> costo: $costoPar | tiempo: $tiempoPar%.2f ms")

    println("\nRuta óptima secuencial: " + piSec.mkString(" -> "))
    println("Ruta óptima paralela:   " + piPar.mkString(" -> "))

    println("\n============================================")
  }
}
