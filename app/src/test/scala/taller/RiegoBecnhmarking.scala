package taller

import org.scalameter._
import scala.util.Random

object RiegoBenchmarking {

  // Tipos definidos en Riego.scala
  type Finca = Riego.Finca
  type Distancia = Riego.Distancia
  type ProgRiego = Riego.ProgRiego

  /** Genera finca, distancia y una programación válida (solo N <= 10). */
  def generarEntradas(n: Int, seed: Int) = {
    val r = new Random(seed)
    val f = Riego.fincaAlAzarconRandom(r, n)
    val d = Riego.distanciaAlAzarconRandom(r, n)
    val pi =
      if (n <= 10)
        Riego.generarProgramaciones(f).head
      else
        (0 until n).toVector // para costos no factoriales
    (f, d, pi)
  }

  /** Imprime tabla comparativa para secuencial vs paralelo */
  def comparar(
                titulo: String,
                tamanos: Vector[Int],
                generador: Int => (Finca, Distancia, ProgRiego),
                secuencial: (Finca, Distancia, ProgRiego) => Any,
                paralelo:   (Finca, Distancia, ProgRiego) => Any
              ): Unit = {

    println("\n=================================================")
    println(s"  $titulo")
    println("=================================================")
    println("Tamaño | Secuencial (ms) | Paralelo (ms) | Aceleración (%)")
    println("-------|-----------------|---------------|-----------------")

    for(n <- tamanos) {
      val (f, d, pi) = generador(n)

      val tSeq = measure { secuencial(f, d, pi) }
      val tPar = measure { paralelo(f, d, pi) }

      val speedup =
        if (tSeq.value > 0)
          ((tSeq.value - tPar.value) / tSeq.value) * 100
        else 0.0

      println(f"$n%6d | ${tSeq.value}%.2f          | ${tPar.value}%.2f       | $speedup%.2f")
    }

    println("-------------------------------------------------\n")
  }

  // ============================================================
  // SECCIÓN 3.1 — COSTOS (no factorial, se permiten tamaños grandes)
  // ============================================================
  def medirCostos(): Unit = {
    val tamanos = Vector(10, 50, 100)

    comparar(
      "3.1 — Costo de Riego",
      tamanos,
      n => generarEntradas(n, 100 + n),
      (f, _, pi) => Riego.costoRiegoFinca(f, pi),
      (f, _, pi) => RiegoParalelo.costoRiegoFincaPar(f, pi)
    )

    comparar(
      "3.1 — Costo de Movilidad",
      tamanos,
      n => generarEntradas(n, 200 + n),
      (f, d, pi) => Riego.costoMovilidad(f, pi, d),
      (f, d, pi) => RiegoParalelo.costoMovilidadPar(f, pi, d)
    )
  }

  // ============================================================
  // SECCIÓN 3.2 — GENERACIÓN DE PROGRAMACIONES (N ≤ 10)
  // ============================================================
  def medirGeneracion(): Unit = {
    val tamanos = Vector(8, 10)

    comparar(
      "3.2 — Generación de Programaciones (N!)",
      tamanos,
      n => generarEntradas(n, 300 + n),
      (f, _, _) => Riego.generarProgramaciones(f),
      (f, _, _) => RiegoParalelo.generarProgramacionesRiegoPar(f)
    )
  }

  // ============================================================
  // SECCIÓN 3.3 — PROGRAMACIÓN ÓPTIMA (N ≤ 10)
  // ============================================================
  def medirOptimo(): Unit = {
    val tamanos = Vector(6, 7, 8)

    comparar(
      "3.3 — Programación Óptima (N!)",
      tamanos,
      n => generarEntradas(n, 500 + n),
      (f, d, _) => Riego.programacionOptima(f, d),
      (f, d, _) => RiegoParalelo.ProgramacionRiegoOptimoPar(f, d)
    )
  }

  // ============================================================
  // MAIN — Ejecuta todo el benchmarking solicitado
  // ============================================================
  def main(args: Array[String]): Unit = {
    println("=================================================")
    println("      BENCHMARKING OFICIAL DEL PROYECTO")
    println("=================================================")

    medirCostos()
    medirGeneracion()
    medirOptimo()

    println("Benchmarking finalizado.")
  }
}

