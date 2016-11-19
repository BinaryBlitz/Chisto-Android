package ru.binaryblitz.Chisto.Utils

interface SwipeToDeleteAdapter {
    fun isPendingRemoval(position: Int): Boolean
    fun remove(position: Int)
    fun pendingRemoval(position: Int)
    fun isUndo(): Boolean
}
