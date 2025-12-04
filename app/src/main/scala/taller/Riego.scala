package taller

import scala.annotation.meta.param
import scala.util.Random
import scala.annotation.tailrec

object Riego {

  // Definimos los tipos
  type Tablon = (Int, Int, Int) // Esto hace referencia al tiempo de supervivencia, tiempo de regado y prioridad
  type Finca = Vector[Tablon] // Este es el vector de tablones
  type Distancia = Vector[Vector[Int]] // Esta es la matriz de distancia entre los tablones
  type ProgRiego = Vector[Int] // Vector para la programacion de riego
  type TiempoInicioRiego = Vector[Int] // Vector con el tiempo de inicio del riego

  // Es el random que utilizamos en algunas funciones
  val random = new Random()

// 2.1 GENERACION DE ENTRADAS ALEATORIAS

  // Generamos una Finca totalmente aleatoria utilizando el random normal
  def fincaAlAzar(long: Int): Finca = // Aqui implementamos el generar numero de tablones. (el parametro long define el numero de tablones)
    Vector.fill(long)(
      (random.nextInt(long * 2) + 1,
        random.nextInt(long) + 1, // Aqui implementamos el generar duracion de riego (Este valor es el tiempo del regado)
        random.nextInt(4) + 1)
    )
  // Generamos una Finca usando un Random dado (con el fin de las pruebas que se puedan repetir)
  def fincaAlAzarconRandom(r: Random, long: Int): Finca = // Aqui tambien implementamos el generar numero de tablones.
    Vector.fill(long)(
      (r.nextInt(long * 2) + 1,
        r.nextInt(long) + 1,
      r.nextInt(4) + 1)
    )

  // Generamos una matriz de distancia simetrica con el Random normal
  def distanciaAlAzar(long: Int): Distancia = { // Aqui implementamos el generar distancias
    val v = Vector.fill(long, long)(random.nextInt(long * 3) + 1)
    Vector.tabulate(long, long)((i, j) =>
    if (i == j) 0
    else if (i < j) v(i)(j)
    else v(j)(i)
    )
  }
  // Generamos una matriz de distancia con el Random pero ahora dado
  def distanciaAlAzarconRandom(r: Random, long: Int): Distancia = { // Aqui tambien implementamos el generar distancias
    val v = Vector.fill(long, long)(r.nextInt(long * 3) * 1)
    Vector.tabulate(long, long)((i, j) =>
    if (i == j) 0
    else if (i < j) v(i)(j)
    else v(j)(i)
    )
  }

// 2.2 EXPLORACION DE ENTRADAS

  // Estos son getters para sacar la informacion de componentes concretos de un tablon
  def tsup(f: Finca, i: Int): Int = f(i)._1 // Tiempo de supervivencia
  def treg(f: Finca, i: Int): Int = f(i)._2 // Tiempo de regado
  def prio(f: Finca, i: Int): Int = f(i)._3 // Prioridad

  // Intentamos mostrar la finca de forma "legible"
  def mostrarFinca(f: Finca): String = {
    val header = "Tablon | Tiempo_supervivencia | Tiempo_regado | Prioridad\n"
    val rows = f.zipWithIndex.map {case ((ts, tr, p), i) =>
    f"   $i%2d    |    $ts%3d    |    $tr%2d    |    $p%1d"
    }.mkString("\n")
    s"$header\n$rows"
  }

  // Intentamos mostrar las distancias de forma "legible"
  def mostrarDistancias(d: Distancia): String = { // Aqui implementamos las distancias
    val n = d.length
    val header = "  |   " + (0 until n).map(i => f"$i%3d").mkString
    val separator = "---+" + ("----" * n)
    val rows = d.zipWithIndex.map {case (fila, i) =>
      f" $i%2d | " + fila.map(v => f"$v%3d").mkString
    }.mkString("\n")
    s"$header\n$separator\n$rows"
  }


// 2.3 CALCULO DEL TIEMPO DE INICIO DEL RIEGO
/*
  @param // Finca con n tablones (f)
  @param // Programacion de riego (pi)
  @return // Vector para t(i) donde este es el tiempo de inicio del tablon i
*/
  // Implementamos tIR que nos ayuda a calcular el tiempo de inicio del riego con recursion de coola.
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








}

