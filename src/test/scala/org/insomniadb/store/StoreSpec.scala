package org.insomniadb.store

import org.scalatest._

class StoreSpec extends FlatSpec with Matchers {
  val description = MorselDescription(1, 0)

  "An empty database" should "return an empty allnodes scan" in {
    Store.empty.nodesGetAllPrimitive(0, description) shouldBe empty
  }

  "A database with 10 nodes" should "return expected morsels" in {

    val allNodes = new Store(highestNodeId = 10).nodesGetAllPrimitive(0, description)

    val morsel = allNodes.next()
    allNodes shouldBe empty
    morsel.longs.take(10) should equal(Array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
  }

  it should "where 5 has been deleted" in {
    val deletedNodes = new java.util.BitSet(100)
    (0 to 4) foreach deletedNodes.set
    val allNodes = new Store(highestNodeId = 10, deletedNodes = deletedNodes).nodesGetAllPrimitive(0, description)

    val morsel = allNodes.next()
    allNodes shouldBe empty
    morsel.longs.take(5) should equal(Array(5, 6, 7, 8, 9))
  }
}