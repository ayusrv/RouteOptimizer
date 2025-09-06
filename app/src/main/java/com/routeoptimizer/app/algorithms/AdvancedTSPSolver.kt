package com.routeoptimizer.app.algorithms

class AdvancedTSPSolver {

    // Dynamic Programming approach for small instances (up to 15-20 cities)
    fun solveTSPDP(distances: Array<DoubleArray>, start: Int = 0): Pair<Double, List<Int>> {
        val n = distances.size
        val dp = Array(1 shl n) { DoubleArray(n) { Double.MAX_VALUE } }
        val parent = Array(1 shl n) { IntArray(n) { -1 } }

        dp[1 shl start][start] = 0.0

        for (mask in 0 until (1 shl n)) {
            for (u in 0 until n) {
                if (dp[mask][u] == Double.MAX_VALUE) continue
                if ((mask and (1 shl u)) == 0) continue

                for (v in 0 until n) {
                    if (mask and (1 shl v) != 0) continue

                    val newMask = mask or (1 shl v)
                    val newCost = dp[mask][u] + distances[u][v]

                    if (newCost < dp[newMask][v]) {
                        dp[newMask][v] = newCost
                        parent[newMask][v] = u
                    }
                }
            }
        }

        // Find minimum cost to return to start
        val finalMask = (1 shl n) - 1
        var minCost = Double.MAX_VALUE
        var lastCity = -1

        for (u in 0 until n) {
            if (u == start) continue
            val cost = dp[finalMask][u] + distances[u][start]
            if (cost < minCost) {
                minCost = cost
                lastCity = u
            }
        }

        // Reconstruct path
        val path = mutableListOf<Int>()
        var mask = finalMask
        var curr = lastCity

        while (curr != -1) {
            path.add(0, curr)
            val prev = parent[mask][curr]
            mask = mask xor (1 shl curr)
            curr = prev
        }

        return Pair(minCost, path)
    }

    // Genetic Algorithm for larger instances
    fun solveTSPGenetic(
        distances: Array<DoubleArray>,
        populationSize: Int = 100,
        generations: Int = 500,
        mutationRate: Double = 0.02
    ): Pair<Double, List<Int>> {
        val n = distances.size
        var population = generateInitialPopulation(n, populationSize)

        repeat(generations) {
            population = evolvePopulation(population, distances, mutationRate)
        }

        val best = population.minByOrNull { calculateTotalDistance(it, distances) }!!
        return Pair(calculateTotalDistance(best, distances), best)
    }

    private fun generateInitialPopulation(n: Int, populationSize: Int): List<List<Int>> {
        val population = mutableListOf<List<Int>>()

        repeat(populationSize) {
            val individual = (0 until n).toMutableList()
            individual.shuffle()
            population.add(individual)
        }

        return population
    }

    private fun evolvePopulation(
        population: List<List<Int>>,
        distances: Array<DoubleArray>,
        mutationRate: Double
    ): List<List<Int>> {
        val newPopulation = mutableListOf<List<Int>>()
        val elite = population.sortedBy { calculateTotalDistance(it, distances) }

        // Keep top 10% as elite
        val eliteSize = population.size / 10
        newPopulation.addAll(elite.take(eliteSize))

        // Generate offspring
        while (newPopulation.size < population.size) {
            val parent1 = selectParent(population, distances)
            val parent2 = selectParent(population, distances)
            val offspring = crossover(parent1, parent2)
            val mutated = if (Math.random() < mutationRate) mutate(offspring) else offspring
            newPopulation.add(mutated)
        }

        return newPopulation
    }

    private fun selectParent(population: List<List<Int>>, distances: Array<DoubleArray>): List<Int> {
        // Tournament selection
        val tournamentSize = 5
        val tournament = population.shuffled().take(tournamentSize)
        return tournament.minByOrNull { calculateTotalDistance(it, distances) }!!
    }

    private fun crossover(parent1: List<Int>, parent2: List<Int>): List<Int> {
        // Order Crossover (OX)
        val n = parent1.size
        val start = (0 until n).random()
        val end = (start + 1..n).random()

        val offspring = MutableList(n) { -1 }

        // Copy segment from parent1
        for (i in start until end) {
            offspring[i] = parent1[i]
        }

        // Fill remaining positions from parent2
        var pos = end % n
        for (city in parent2) {
            if (city !in offspring) {
                while (offspring[pos] != -1) {
                    pos = (pos + 1) % n
                }
                offspring[pos] = city
            }
        }

        return offspring
    }

    private fun mutate(individual: List<Int>): List<Int> {
        val mutated = individual.toMutableList()
        val i = (0 until mutated.size).random()
        val j = (0 until mutated.size).random()

        // Swap mutation
        mutated[i] = mutated[j].also { mutated[j] = mutated[i] }

        return mutated
    }

    private fun calculateTotalDistance(tour: List<Int>, distances: Array<DoubleArray>): Double {
        var total = 0.0
        for (i in tour.indices) {
            val from = tour[i]
            val to = tour[(i + 1) % tour.size]
            total += distances[from][to]
        }
        return total
    }
}