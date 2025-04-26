package com.example.mobile_phone_subscription;

    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.Query;

    /**
     * Segédosztály a csomagok Firestore-ban történő szűréséhez és rendezéséhez.
     */
    public class PlanFilterHelper {

        private final FirebaseFirestore firestore;

        /**
         * Konstruktor, inicializálja a Firestore példányt.
         */
        public PlanFilterHelper() {
            this.firestore = FirebaseFirestore.getInstance();
        }

        /**
         * Szűrés ár alapján, minimum és maximum ár megadásával, eredmények limitálása.
         * @param minPrice Minimum ár
         * @param maxPrice Maximum ár
         * @param limit Eredmények maximális száma
         * @return Firestore lekérdezés a szűrt csomagokra
         */
        public Query filterByPriceRangeWithLimit(int minPrice, int maxPrice, int limit) {
            return firestore.collection("plans")
                    .whereGreaterThanOrEqualTo("price", minPrice)
                    .whereLessThanOrEqualTo("price", maxPrice)
                    .orderBy("price")
                    .limit(limit);
        }

        /**
         * Szűrés név alapján, részleges egyezéssel, eredmények limitálása.
         * @param name A csomag neve vagy részlete
         * @param limit Eredmények maximális száma
         * @return Firestore lekérdezés a szűrt csomagokra
         */
        public Query filterByNameWithLimit(String name, int limit) {
            return firestore.collection("plans")
                    .whereGreaterThanOrEqualTo("name", name)
                    .whereLessThanOrEqualTo("name", name + "\uf8ff")
                    .orderBy("name")
                    .limit(limit);
        }

        /**
         * Rendezés népszerűség szerint (feliratkozók száma alapján, csökkenő sorrend).
         * @param limit Eredmények maximális száma
         * @return Firestore lekérdezés a rendezett csomagokra
         */
        public Query sortByPopularity(int limit) {
            return firestore.collection("plans")
                    .orderBy("subscribers", Query.Direction.DESCENDING)
                    .limit(limit);
        }

        /**
         * Rendezés ár szerint csökkenő sorrendben (drágától az olcsóig).
         * @param limit Eredmények maximális száma
         * @return Firestore lekérdezés a rendezett csomagokra
         */
        public Query sortByPriceDescending(int limit) {
            return firestore.collection("plans")
                    .orderBy("price", Query.Direction.DESCENDING)
                    .limit(limit);
        }

        /**
         * Rendezés ár szerint növekvő sorrendben (olcsótól a drágáig).
         * @param limit Eredmények maximális száma
         * @return Firestore lekérdezés a rendezett csomagokra
         */
        public Query sortByPriceAscending(int limit) {
            return firestore.collection("plans")
                    .orderBy("price", Query.Direction.ASCENDING)
                    .limit(limit);
        }
    }