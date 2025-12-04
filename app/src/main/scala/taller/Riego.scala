package taller

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
  def distanciaAlAzarconRandom(r: Random, long: Int): Distancia = {
    val v = Vector.fill(long, long)(r.nextInt(long * 3) * 1)
    Vector.tabulate(long, long)((i, j) =>
    if (i == j) 0
    else if (i < j) v(i)(j)
    else v(j)(i)
    )
  }






}

