package no.nav.hm.grunndata.index.agreement

class AgreementLabels {

    companion object {
        fun matchTitleToLabel(title: String): String {
            labels.forEach {
                if (title.contains(it.subString, ignoreCase = true)) return it.name
            }
            return "Annet"
        }

        private val labels = listOf(
            AgreementLabel("Manuelle rullestoler", "Manuelle rullestoler"),
            AgreementLabel("Elektriske rullestoler", "Elektriske rullestoler"),
            AgreementLabel("Hygienehjelpemidler", "Hygienehjelpemidler og støttestang"),
            AgreementLabel("Sitteputer", "Sitteputer"),
            AgreementLabel("Arbeidsstoler", "Stoler og bord"),
            AgreementLabel("Ganghjelpemidler", "Ganghjelpemidler"),
            AgreementLabel("Senger", "Senger"),
            AgreementLabel("Stoler med oppreisingsfunksjon", "Stoler med oppreisingsfunksjon"),
            AgreementLabel("Kalendere", "Kalendere"),
            AgreementLabel("Sykler", "Sykler"),
            AgreementLabel("overflytting, vending og posisjonering", "Overflytting, vending og posisjonering"),
            AgreementLabel("kjøkken og bad", "Innredning kjøkken og bad"),
            AgreementLabel("Omgivelseskontroll", "Omgivelseskontroll"),
            AgreementLabel("Vogner", "Vogner og aktivitetshjelpemidler"),
            AgreementLabel("Høreapparater", "Høreapparater"),
            AgreementLabel("Madrasser med trykksårforebyggende egenskaper", "Madrasser"),
            AgreementLabel("hjelpemidler til trapp", "Hjelpemidler til trapp"),
            AgreementLabel("Varsling", "Varsling"),
            AgreementLabel("Kommunikasjon", "Kommunikasjon"),
            AgreementLabel("Kjøreposer", "Kjøreposer, varmeposer og regncape"),
            AgreementLabel("Ståstativ", "Ståstativ og treningshjelpemidler"),
            AgreementLabel("Hørsel", "Hørsel"),
            AgreementLabel("Syn", "Syn"),
            AgreementLabel("personløftere", "Plattformer og personløftere"),
            AgreementLabel("Varmehjelpemidler", "Varmehjelpemidler"),
            AgreementLabel("Biler", "Biler"),
            AgreementLabel("Sittesystem", "Sittesystem"),
            AgreementLabel("Førerhunder", "Førerhunder og servicehunder"),
            AgreementLabel("Kjøreramper", "Kjøreramper"),
            AgreementLabel("Bilombygg", "Bilombygg"),
            AgreementLabel("seksuallivet", "Seksuallivet")
        )


    }
}

data class AgreementLabel(val subString: String, val name: String)