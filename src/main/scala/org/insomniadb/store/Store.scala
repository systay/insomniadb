package org.insomniadb.store

import java.util

import org.agrona.collections.Int2ObjectHashMap

object Store {
  def empty: Store = new Store
}

class Store(val highestNodeId: Int = -1,
            val highestEdgeId: Int = -1,

            // Keeps track of deleted nodes/relationships
            val deletedNodes: util.BitSet = new util.BitSet(),

            val deletedEdges: util.BitSet = new util.BitSet(),

            // Tokens
            val stringToToken: util.Map[String, Int] = new util.HashMap[String, Int](),

            // For each node, we keep an array with all the labels a node contains
            val node_labels: Array[Array[Int]] = new Array[Array[Int]](0),

            // For each relationship, keep track of it's type
            val edge_types: Array[Long] = new Array[Long](0),

            // We store all properties values for a single token in one map
            val propertyValues: Int2ObjectHashMap[util.HashMap[Long, CypherValue]] = new Int2ObjectHashMap[util.HashMap[Long, CypherValue]]()
           ) extends StoreSPI {

  type TOKEN = Int
  type ID = Long
  val tokenToString: Int2ObjectHashMap[String] = {
    val result = new Int2ObjectHashMap[String]()
    stringToToken.forEach((t: String, u: TOKEN) => result.put(u, t))
    result
  }

  // Nodes & relationships are represented by a long.
  // Negative numbers are relationships, and positive are nodes

  // Marker of the highest already used id for nodes/relationships

  override def nodesGetAllPrimitive(offset: Int, description: MorselDescription): Iterator[Morsel] =
    new Iterator[Morsel] {
      private var currentNodeId = -1

      override def hasNext: Boolean = this.currentNodeId < highestNodeId

      override def next(): Morsel =
        if (isEmpty)
          Iterator.empty.next()
        else {
          val output = createMorsel(description)
          var currentRow = -1

          while (currentNodeId < highestNodeId && currentRow < output.numberOfRows) {
            currentNodeId += 1
            if (!deletedNodes.get(currentNodeId)) {
              currentRow += 1
              val offsetIntoArray = currentRow * description.longsPerRow + offset
              output.longs(offsetIntoArray) = currentNodeId
            }
          }

          output.highestUsed = currentRow
          output
        }

    }

  private def createMorsel(description: MorselDescription): Morsel = new Morsel(100, description)
}

case class MorselDescription(longsPerRow: Int, valuesPerRow: Int)

trait StoreSPI {
  def nodesGetAllPrimitive(offset: Int, description: MorselDescription): Iterator[Morsel]
}

class Morsel(val numberOfRows: Int, description: MorselDescription) {
  val longs: Array[Int] = new Array[Int](numberOfRows * description.longsPerRow)
  val values: Array[CypherValue] = new Array[CypherValue](numberOfRows * description.valuesPerRow)
  // The row with the highest index can be found at this offset
  var highestUsed: Int = -1

  // A full morsel is a signal that more data can be had
  def moreToCome: Boolean = highestUsed == numberOfRows - 1
}

trait CypherValue
