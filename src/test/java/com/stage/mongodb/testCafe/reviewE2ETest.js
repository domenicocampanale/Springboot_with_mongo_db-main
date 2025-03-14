import { Selector } from 'testcafe';

fixture `Review E2E Tests`
    .page `http://localhost:8080/view/review/home`;

let reviewId;
let movieId;

test('Reset Database', async t => {
    await fetch('http://localhost:8080/view/movie/reset', { method: 'GET' });
    await fetch('http://localhost:8080/view/review/reset', { method: 'GET' });
});

// Naviga alla pagina specificata
const navigateTo = async (t, path) => {
    await t.navigateTo(`http://localhost:8080/view/review/${path}`);
};

const navigateToMovie = async (t, path) => {
    await t.navigateTo(`http://localhost:8080/view/movie/${path}`);
};

// Verifica il titolo della pagina
const verifyPageTitle = async (t, expectedTitle) => {
    const title = await Selector('title').innerText;
    await t.expect(title).contains(expectedTitle);
};

// Trova un elemento usando un selettore
const findElement = (selector) => Selector(selector);

// Clicca su un pulsante specifico
const clickButton = async (t, selector) => {
    const button = findElement(selector);
    await t.click(button);
};

test('Test Home Page', async t => {
    await verifyPageTitle(t, 'Reviews Home Page');
});

test('Test Add Review', async t => {
    // Aggiungi un film per assicurarti che ci siano film disponibili
    await navigateToMovie(t, 'add');  // Usa la funzione per navigare ai film
    await clickButton(t, 'button[type="submit"]');

    // Verifica che la navigazione avvenga verso la pagina della lista film dopo l'aggiunta
    const currentUrlAfterAddMovie = await t.eval(() => window.location.href);
    await t.expect(currentUrlAfterAddMovie).contains('list'); // La pagina della lista dei film

    // Ottieni l'ID di un film esistente dalla lista dei film
    await navigateToMovie(t, 'list');  // Naviga alla lista dei film
    const movieListItems = Selector('li');
    movieId = await movieListItems.nth(0).child('p').withText('Movie ID:').child('span').innerText;

    // Ora aggiungi una recensione per il film appena aggiunto
    await navigateTo(t, 'add');  // Naviga alla pagina di aggiunta recensione

    // Svuota il campo movieId prima di inserire il nuovo ID del film
    await t.selectText('#movieId').pressKey('delete');
    await t.typeText('#movieId', movieId);  // Usa l'ID del film appena aggiunto

    await clickButton(t, 'button[type="submit"]');

    // Verifica che la recensione sia stata aggiunta correttamente
    const currentUrlAfterAddReview = await t.eval(() => window.location.href);
    await t.expect(currentUrlAfterAddReview).contains('list'); // La pagina della lista delle recensioni
});



test('Test Review List', async t => {
    // Visualizza la lista delle recensioni
    await navigateTo(t, 'list');
    const reviewListItems = Selector('li');
    await t.expect(reviewListItems.exists).ok('La lista delle recensioni non è stata trovata.');

    // Estrai l'ID della recensione dalla lista
    const firstReviewItem = reviewListItems.nth(0);
    const reviewIdElement = firstReviewItem.child('p').withText('Review ID:');
    reviewId = await reviewIdElement.child('span').innerText;
});

test('Test Edit Review', async t => {
    // Modifica una recensione esistente
    await navigateTo(t, `edit?id=${reviewId}`);
    await t.selectText('#comment').pressKey('delete').typeText('#comment', 'Updated Comment!');
    await clickButton(t, 'button[type="submit"]');
    const currentUrlAfterEdit = await t.eval(() => window.location.href);
    await t.expect(currentUrlAfterEdit).contains('list?success');
});

test('Test Review Details', async t => {
    // Verifica i dettagli di una recensione
    await navigateTo(t, `details?id=${reviewId}`);

    // Modifica il selettore per il commento
    const commentText = findElement('.movie-details p').withText('Comment:').child('span');
    const ratingText = findElement('.movie-details p').withText('Rating:').child('span');

    // Verifica che i dettagli esistano nella pagina
    await t.expect(commentText.exists).ok();
    await t.expect(ratingText.exists).ok();
});

test('Test Review Not Found', async t => {
    // Verifica la gestione di una recensione non trovata
    await navigateTo(t, 'details?id=999');
    const errorElement = findElement('.error');
    await t.expect(errorElement.exists).ok();
});

test('Test Edit Review Not Found', async t => {
    // Verifica la gestione di una recensione non trovata durante la modifica
    await navigateTo(t, 'edit?id=999');
    const errorElement = findElement('.error');
    await t.expect(errorElement.exists).ok();
});

test('Test Delete Review Not Found', async t => {
    // Verifica la gestione di una recensione non trovata durante la cancellazione
    await navigateTo(t, 'delete?id=999');
    const errorElement = findElement('.error');
    await t.expect(errorElement.exists).ok();
});
/*
test('Test Add Review Client Validation', async t => {
    // Verifica la validazione lato client per aggiungere una recensione senza commento
    await navigateTo(t, 'add');

    // Svuota il campo commento (se necessario) e inserisci una stringa vuota per la validazione
    await t.typeText('#comment', '');

    // Svuota il campo movieId
    await t.selectText('#movieId'); // Seleziona tutto il testo nel campo movieId
    await t.pressKey('backspace'); // Rimuovi il testo dal campo

    // Aggiungi il movieId del film che hai creato
    await t.typeText('#movieId', movieId); // Inserisci il movieId del film creato in precedenza

    // Aggiungi il pulsante di submit
    await clickButton(t, 'button[type="submit"]');

    // Controlla che il form non venga inviato (verifica che l'URL non cambi)
    const currentUrlAfterValidation = await t.eval(() => window.location.href);
    await t.expect(currentUrlAfterValidation).contains('add');
});*/



test('Test Add Review With Invalid Data', async t => {
    // Aggiungi una recensione con un punteggio non valido
    await navigateTo(t, 'add');
    await t.typeText('#movieId', movieId);
    await t.typeText('#comment', 'Test review');
    await t.typeText('#rating', '100');  // Punteggio invalido
    await clickButton(t, 'button[type="submit"]');
    const errorElement = findElement('.error');
    await t.expect(errorElement.exists).ok();
});

test('Test Edit Review With Invalid Data', async t => {
    // Modifica una recensione con un punteggio non valido
    await navigateTo(t, `edit?id=${reviewId}`);
    await t.typeText('#rating', '100');  // Punteggio invalido
    await clickButton(t, 'button[type="submit"]');
    const errorElement = findElement('.error');
    await t.expect(errorElement.exists).ok();
});

test('Test Delete Review', async t => {
    // Elimina una recensione
    await navigateTo(t, `delete?id=${reviewId}`);
    const currentUrlAfterDelete = await t.eval(() => window.location.href);
    await t.expect(currentUrlAfterDelete).contains('list?success');
});
