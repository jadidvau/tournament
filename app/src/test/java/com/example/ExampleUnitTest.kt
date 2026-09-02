package com.example

import com.example.data.model.TournamentRules
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun defaultTournamentRules_hasExpectedBadges() {
    val rules = TournamentRules()
    assertEquals("10 Mins", rules.matchDuration)
    assertEquals("ET/PK ON", rules.extraTimePk)
    assertEquals("5 Subs", rules.substitutions)
    assertEquals("15-min rematch rule", rules.rematchRule)
    assertEquals("10-min walkover grace", rules.walkoverGrace)
  }

  @Test
  fun updatedTournamentRules_updatesValuesCorrectly() {
    val initial = TournamentRules()
    val updated = initial.copy(
      matchDuration = "12 Mins",
      extraTimePk = "ET/PK OFF",
      substitutions = "3 Subs"
    )
    assertEquals("12 Mins", updated.matchDuration)
    assertEquals("ET/PK OFF", updated.extraTimePk)
    assertEquals("3 Subs", updated.substitutions)
  }
}
