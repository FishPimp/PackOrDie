# PackMaster 🧳

Den ultimata smarta packlist-appen för Android – för både vardagliga rutiner
(gymmet, barnvagnen till förskolan, badhuset) och flerdagsresor.

Byggd i **Kotlin** med **Jetpack Compose**, **Material 3** och **Room**, enligt
**Clean Architecture** (MVVM). Hela gränssnittet är på svenska och alla texter
ligger i `res/values/strings.xml` (förvalda databasobjekt i `strings_seed.xml`).

## Funktioner

| Område | Innehåll |
| --- | --- |
| **Masterkatalog** | Förvalda svenska kategorier (Kläder, Hygienartiklar, Elektronik, Barn/Baby, Dokument, Träningskläder …) och drygt 50 objekt. Egna objekt, egna kategorier, emoji-ikoner och egna bilder (fotoväljare). |
| **Profiler** | Tagga objekt per person, t.ex. "Vuxen 1", "Barn 1", "Gemensamt". Profiler kan döpas om, läggas till och tas bort. |
| **Vardagsväskor** | För återkommande rutiner. Knappen **Återställ packlista** bockar av allt inför nästa gång (med Ångra). |
| **Reseväskor** | Start-/slutdatum eller antal dagar. Antal per objekt räknas med regler: *fast antal*, *per dag* (t.ex. 4 blöjor/dag) eller *per natt*. Med **tvättmöjlighet** begränsas dagliga kläder till max 7 dagar. |
| **Underväskor** | Dela upp packningen i t.ex. Kabinväska, Hygienväska och Barnens ryggsäck, med egen maxvikt. |
| **Checklista** | Gruppera efter kategori, underväska eller person. Framstegsmätare (t.ex. 14/25 saker packade), haptisk feedback vid avbockning och filtret "Visa alla"/"Visa endast opackade". |
| **Viktbudget** | Valfri maxvikt per väska (t.ex. 23 kg) och uppskattad vikt per objekt. Varning när budgeten överskrids. |
| **Design** | Lekfull Material 3-palett med ljust och mörkt läge (följ system, ljust eller mörkt). Offline-first med Room. |

## Arkitektur

```
domain/   Ren Kotlin-modul: modeller, repository-gränssnitt och use cases
          (QuantityCalculator, BuildPackingListUseCase, ResetPackingListUseCase …)
app/
  data/   Room (entities, DAO:er, databas), repository-implementationer,
          förvald data (DatabaseSeeder), bildlagring och inställningar
  ui/     Compose-skärmar + ViewModels (Väskor, Packning, Katalog, Profiler)
```

Domänlogiken (antalsberäkning, tvättbegränsning, gruppering, framsteg och vikt)
är enhetstestad i `domain/src/test`.

## Bygga

Kräver JDK 17 och Android SDK (compileSdk 35).

```bash
./gradlew :domain:test          # enhetstester
./gradlew :app:assembleDebug    # app/build/outputs/apk/debug/app-debug.apk
```

## GitHub Actions

`.github/workflows/build.yml` kör tester och bygger `app-debug.apk` vid varje push
till `main` (samt för pull requests och manuellt). APK:n laddas upp som
workflow-artefakten **app-debug**.
