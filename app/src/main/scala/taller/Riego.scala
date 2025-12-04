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
}

