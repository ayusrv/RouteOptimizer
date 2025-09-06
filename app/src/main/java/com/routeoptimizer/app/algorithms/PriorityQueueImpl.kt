package com.routeoptimizer.app.algorithms

class PriorityQueueImpl<T : Comparable<T>> {
    private val heap = mutableListOf<T>()

    val size: Int get() = heap.size
    val isEmpty: Boolean get() = heap.isEmpty()

    fun add(element: T) {
        heap.add(element)
        heapifyUp(heap.size - 1)
    }

    fun poll(): T? {
        if (heap.isEmpty()) return null

        val result = heap[0]
        heap[0] = heap.last()
        heap.removeAt(heap.size - 1)

        if (heap.isNotEmpty()) {
            heapifyDown(0)
        }

        return result
    }

    fun peek(): T? = heap.firstOrNull()

    private fun heapifyUp(index: Int) {
        if (index == 0) return

        val parentIndex = (index - 1) / 2
        if (heap[index] < heap[parentIndex]) {
            heap[index] = heap[parentIndex].also { heap[parentIndex] = heap[index] }
            heapifyUp(parentIndex)
        }
    }

    private fun heapifyDown(index: Int) {
        val leftChild = 2 * index + 1
        val rightChild = 2 * index + 2
        var smallest = index

        if (leftChild < heap.size && heap[leftChild] < heap[smallest]) {
            smallest = leftChild
        }

        if (rightChild < heap.size && heap[rightChild] < heap[smallest]) {
            smallest = rightChild
        }

        if (smallest != index) {
            heap[index] = heap[smallest].also { heap[smallest] = heap[index] }
            heapifyDown(smallest)
        }
    }
}