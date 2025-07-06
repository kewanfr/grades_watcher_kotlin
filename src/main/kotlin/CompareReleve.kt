package fr.kewan

class CompareReleve {
    companion object {
        fun findNoteChanges(oldData: ReleveData, newData: ReleveData): List<NoteDifference> {
            val changes = mutableListOf<NoteDifference>()

            // Comparer les ressources
            for ((id, newRessource) in newData.ressources) {
                val oldRessource = oldData.ressources[id]
                if (oldRessource != null) {
                    for (newEval in newRessource.evaluations) {
                        val oldEval = oldRessource.evaluations.find { it.id == newEval.id }

                        val isNew = oldEval?.note?.value != newEval.note.value && !(oldEval?.note?.value == "~" && newEval.note.value == "~")
                        if (isNew || (oldEval == null && newEval.note.value != "~")) {

                                changes.add(
                                    NoteDifference(
                                        id = id,
                                        title = newRessource.titre,
                                        description = newEval.description,
                                        oldValue = oldEval?.note?.value ?: "~",
                                        newValue = newEval.note.value,
                                        oldUserMoy = oldData.semestre.notes.value,
                                        newUserMoy = newData.semestre.notes.value,
                                        oldUserRank = oldData.semestre.rang.value,
                                        newUserRank = newData.semestre.rang.value,

                                        oldPromoMoy = oldData.semestre.notes.moy,
                                        newPromoMoy = newData.semestre.notes.moy,
                                        newMin = newEval.note.min,
                                        newMax = newEval.note.max,
                                        newMoy = newEval.note.moy,
                                        isNew = isNew
                                    )
                                )
                    }
                        /*if (oldEval != null && oldEval.note.value != newEval.note.value &&
                        !(oldEval.note.value == "~" && newEval.note.value == "~")
                    ) {

                        changes.add("$id - ${newRessource.titre} (${newEval.description}): ${oldEval.note.value} → ${newEval.note.value}")
                    } else if (oldEval == null && newEval.note.value != "~") {
                        changes.add("$id - ${newRessource.titre} (${newEval.description}): Nouvelle note → ${newEval.note.value}")
                    }*/
                    }
                }
            }

            // Comparer les SAEs
            for ((id, newSae) in newData.saes) {
                val oldSae = oldData.saes[id]
                if (oldSae != null) {
                    for (newEval in newSae.evaluations) {
                        val oldEval = oldSae.evaluations.find { it.id == newEval.id }
val isNew = oldEval?.note?.value != newEval.note.value && !(oldEval?.note?.value == "~" && newEval.note.value == "~")
                        if (isNew || (oldEval == null && newEval.note.value != "~")) {
                            changes.add(
                                NoteDifference(
                                    id = id,
                                    title = newSae.titre,
                                    description = newEval.description,
                                    oldValue = oldEval?.note?.value ?: "~",
                                    newValue = newEval.note.value,
                                    oldUserMoy = oldData.semestre.notes.value,
                                    newUserMoy = newData.semestre.notes.value,
                                    oldUserRank = oldData.semestre.rang.value,
                                    newUserRank = newData.semestre.rang.value,

                                    oldPromoMoy = oldData.semestre.notes.moy,
                                    newPromoMoy = newData.semestre.notes.moy,
                                    newMin = newEval.note.min,
                                    newMax = newEval.note.max,
                                    newMoy = newEval.note.moy,
                                    isNew = isNew
                                )
                            )
                        }

                        /*if (oldEval != null && oldEval.note.value != newEval.note.value &&
                        !(oldEval.note.value == "~" && newEval.note.value == "~")
                    ) {
                        changes.add("$id - ${newSae.titre} (${newEval.description}): ${oldEval.note.value} → ${newEval.note.value}")
                    } else if (oldEval == null && newEval.note.value != "~") {
                        changes.add("$id - ${newSae.titre} (${newEval.description}): Nouvelle note → ${newEval.note.value}")
                    }*/
                    }
                }
            }

            return changes
        }

        fun textMailNoteDiff(
            note: NoteDifference
        ): Pair<String, String> {
            val obj = "📚 [NOTE] ${note.newValue} - ${note.id} ${note.title} (${note.description})"

            val content = StringBuilder()
            content.append("${note.id} ${note.title} - <b style=\"text-decoration: underline;\">${note.description}</b> => <b style=\"text-decoration: underline;\">${note.newValue}</b>\n")

            if (note.isNew) {
                content.append("Ancienne note : ${note.oldValue} -> ${note.newValue}\n")
            }

            val moyEmoji = if (note.newUserMoy?.toDouble() ?: 0.0 > note.oldUserMoy?.toDouble() ?: 0.0) "▲" else "▼"
            content.append("\nMa moyenne: <b style=\"text-decoration: underline;\">${note.newUserMoy}</b> $moyEmoji (${note.oldUserMoy})\n")

            val rankEmoji = if (note.newUserRank?.toDouble() ?: 0.0 > note.oldUserRank?.toDouble() ?: 0.0) "▲" else "▼"
            content.append("Mon rang: ${note.newUserRank} $rankEmoji (${note.oldUserRank})\n")

            val promoMoyEmoji =
                if (note.newPromoMoy?.toDouble() ?: 0.0 > note.oldPromoMoy?.toDouble() ?: 0.0) "▲" else "▼"
            content.append("\nMoyenne promo: ${note.newPromoMoy} $promoMoyEmoji (${note.oldPromoMoy})\n")
            content.append("Min: ${note.newMin}, Max: ${note.newMax}, Moyenne: ${note.newMoy}\n")

            return Pair(obj, content.toString())
        }

        fun discordEmbedNoteDiffPromo(
            note: NoteDifference
        ): String {

            val promoMoyEmoji =
                if (note.newPromoMoy?.toDouble() ?: 0.0 > note.oldPromoMoy?.toDouble() ?: 0.0) "▲" else "▼"


            val color = 0x5865f2

            val embed = StringBuilder()
            embed.append("{\n")
            embed.append("  \"color\": $color,\n")
            embed.append("  \"title\": \"📚 Nouvelle Note | ${note.id} ${note.title} - ${note.description}\",\n")
            embed.append("  \"description\": \"${note.title}${if (note.description.isNotEmpty()) " - ${note.description}" else ""}\\n\\n")
            embed.append("Moyenne de la promo : **${note.newPromoMoy}** ${promoMoyEmoji} (${note.oldPromoMoy})\",\n")
            embed.append("  \"fields\": [\n")
            embed.append("    {\n")
            embed.append("      \"name\": \"Moyenne\",\n")
            embed.append("      \"value\": \"${note.newMoy ?: "N/A"}\",\n")
            embed.append("      \"inline\": true\n")
            embed.append("    },\n")
            embed.append("    {\n")
            embed.append("      \"name\": \"Minimum / Maximum\",\n")
            embed.append("      \"value\": \"${note.newMin ?: "N/A"} / ${note.newMax ?: "N/A"}\",\n")
            embed.append("      \"inline\": true\n")
            embed.append("    }\n") // Pas de virgule ici
            embed.append("  ]\n")
            embed.append("}\n")

            return embed.toString()
        }

        fun discordEmbedNoteDiffUser(
            note: NoteDifference
        ): String {

            val moyEmoji = if (note.newUserMoy?.toDouble() ?: 0.0 > note.oldUserMoy?.toDouble() ?: 0.0) "▲" else "▼"
            val rankEmoji = if (note.newUserRank?.toDouble() ?: 0.0 > note.oldUserRank?.toDouble() ?: 0.0) "▲" else "▼"
            val promoMoyEmoji =
                if (note.newPromoMoy?.toDouble() ?: 0.0 > note.oldPromoMoy?.toDouble() ?: 0.0) "▲" else "▼"


            val color = 0x5865f2

            val embed = StringBuilder()
            embed.append("{\n")
            embed.append("  \"color\": $color,\n")
            embed.append("  \"title\": \"📚 [NOTE] ${note.id} ${note.title} - ${note.description}\",\n")
            embed.append("  \"description\": \"${note.title}${if (note.description.isNotEmpty()) " - ${note.description}" else ""}\\n\\n")
            embed.append("Note: ${if (note.isNew) "" else "${note.oldValue} -> "} **${note.newValue}**\\n")
            embed.append("Ma Moyenne : **${note.newUserMoy}** ${moyEmoji} (${note.oldUserMoy})\\n\\n")
            embed.append("Mon Rang : **#${note.newUserRank}** ${rankEmoji} (${note.oldUserRank})\\n\\n")
            embed.append("Moyenne de la promo : **${note.newPromoMoy}** ${promoMoyEmoji} (${note.oldPromoMoy})\",\n")
            embed.append("  \"fields\": [\n")
            embed.append("    {\n")
            embed.append("      \"name\": \"Moyenne\",\n")
            embed.append("      \"value\": \"${note.newMoy}\",\n")
            embed.append("      \"inline\": true\n")
            embed.append("    },\n")
            embed.append("    {\n")
            embed.append("      \"name\": \"Minimum / Maximum\",\n")
            embed.append("      \"value\": \"${note.newMin} / ${note.newMax}\",\n")
            embed.append("      \"inline\": true\n")
            embed.append("    }\n")
            embed.append("  ]\n")
            embed.append("}\n")

            return embed.toString()
        }
    }
}

data class NoteDifference(
    val id: String,
    val title: String,
    val description: String,
    val oldValue: String,
    val newValue: String,
    val isNew: Boolean = false,
    val oldUserMoy: String? = null,
    val newUserMoy: String? = null,
    val oldUserRank: String? = null,
    val newUserRank: String? = null,

    val oldPromoMoy: String? = null,
    val newPromoMoy: String? = null,
    val newMin: String? = null,
    val newMax: String? = null,
    val newMoy: String? = null
)