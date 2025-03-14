import {Selector} from 'testcafe';

fixture `Movie E2E Tests`
    .page `http://localhost:8080/view/movie/home`;

test('Reset Database', async t => {
    await fetch('http://localhost:8080/view/movie/reset', { method: 'GET' });
});

let movieId;

const navigateTo = async (t, path) => {
    await t.navigateTo(`http://localhost:8080/view/movie/${path}`);
};

const verifyPageTitle = async (t, expectedTitle) => {
    const title = await Selector('title').innerText;
    await t.expect(title).contains(expectedTitle);
};

const findElement = (selector) => Selector(selector);

const clickButton = async (t, selector) => {
    const button = findElement(selector);
    await t.click(button);
};

test('Test Home Page', async t => {
    await verifyPageTitle(t, 'Movie Home Page');
});

test('Test Add Movie', async t => {
    await navigateTo(t, 'add');
    await clickButton(t, 'button[type="submit"]');
    const currentUrl = await t.eval(() => window.location.href);
    await t.expect(currentUrl).contains('list?success');
});

test('Test Movie List', async t => {
    await navigateTo(t, 'list');

    // Verifica che la lista di film sia presente
    const movieListItems = Selector('li'); // Seleziona tutti gli <li> che contengono i film
    await t.expect(movieListItems.exists).ok('La lista dei film non è stata trovata.');

    // Prendi il primo film nella lista e verifica che l'ID e il titolo siano visibili
    const firstMovieItem = movieListItems.nth(0); // Seleziona il primo elemento <li>
    const movieIdElement = firstMovieItem.child('p').withText('Movie ID:');
    const movieTitleElement = firstMovieItem.child('p').withText('Title:');

    await t.expect(movieIdElement.exists).ok('L\'ID del film non è stato trovato.');
    await t.expect(movieTitleElement.exists).ok('Il titolo del film non è stato trovato.');

    // Estrai l'ID del film
    movieId = await movieIdElement.child('span').innerText;
});


test('Test Edit Movie', async t => {
    await navigateTo(t, `edit?id=${movieId}`);
    const titleField = findElement('#title');
    await t.selectText(titleField).pressKey('delete').typeText(titleField, 'Film Modificato TestCafe');
    await clickButton(t, 'button[type="submit"]');
    const currentUrl = await t.eval(() => window.location.href);
    await t.expect(currentUrl).contains('list?success');
});

test('Test Movie Details', async t => {
    await navigateTo(t, `details?id=${movieId}`);

    const titleText = findElement('.movie-details p').withText('Title:');
    const releaseDateText = findElement('.movie-details p').withText('Release Date:');

    await t.expect(titleText.exists).ok();
    await t.expect(releaseDateText.exists).ok();
});


test('Test Movie Not Found', async t => {
    await navigateTo(t, 'details?id=999');
    const errorElement = findElement('.error');
    await t.expect(errorElement.exists).ok();
});

test('Test Edit Movie Not Found', async t => {
    await navigateTo(t, 'edit?id=999');
    const errorElement = findElement('.error');
    await t.expect(errorElement.exists).ok();
});

test('Test Delete Movie Not Found', async t => {
    await navigateTo(t, 'delete?id=999');
    const errorElement = findElement('.error');
    await t.expect(errorElement.exists).ok();
});

test('Test Add Movie Client Validation', async t => {
    await navigateTo(t, 'add');
    const titleField = findElement('#title');
    await t.selectText(titleField).pressKey('delete');  // Svuota il campo del titolo
    await clickButton(t, 'button[type="submit"]');
    const currentUrl = await t.eval(() => window.location.href);
    await t.expect(currentUrl).contains('add');  // Verifica che l'URL contenga 'add' (la pagina non è cambiata)
});

test('Test Add Movie With Invalid Data', async t => {
    await navigateTo(t, 'add');
    const titleField = findElement('#title');
    const releaseDateField = findElement('#releaseDate');
    await t.typeText(titleField, 'Test').typeText(releaseDateField, 'invalid-date');
    await clickButton(t, 'button[type="submit"]');
    const errorElement = findElement('div.error');
    await t.expect(errorElement.exists).ok();  // Verifica che l'errore sia presente
});

test('Test Edit Movie With Invalid Data', async t => {
    // Naviga alla pagina di modifica del film con un ID valido
    await navigateTo(t, `edit?id=${movieId}`);

    // Attende che la pagina venga caricata controllando la presenza dell'intestazione "Update Movie"
    const pageHeader = Selector('h1').withText('Update Movie');
    await t.expect(pageHeader.exists).ok({ timeout: 5000 });

    // Verifica che il campo "title" sia presente e visibile
    const titleField = Selector('#title');
    await t.expect(titleField.exists).ok('Il campo title non è stato trovato.');
    await t.expect(titleField.visible).ok('Il campo title non è visibile.');

    // Verifica che il campo "releaseDate" sia presente e visibile
    const releaseDateField = Selector('#releaseDate');
    await t.expect(releaseDateField.exists).ok('Il campo releaseDate non è stato trovato.');
    await t.expect(releaseDateField.visible).ok('Il campo releaseDate non è visibile.');

    // Inserisce dati non validi per forzare l'errore di validazione
    await t.typeText(titleField, 'Test')
        .typeText(releaseDateField, 'invalid-date');

    // Clicca sul pulsante di submit
    const submitButton = Selector('button[type="submit"]');
    await t.click(submitButton);

    // Verifica che venga mostrato un messaggio di errore
    const errorElement = Selector('.error');
    await t.expect(errorElement.exists).ok('Il messaggio di errore non è stato visualizzato.');
});

test('Test Patch Movie', async t => {
    await navigateTo(t, `patch?id=${movieId}`);
    const fieldToUpdate = findElement('#fieldToUpdate');
    const valueField = findElement('#updateValue');
    await t.typeText(fieldToUpdate, 'title')
        .typeText(valueField, 'Film Aggiornato con PATCH');
    await clickButton(t, 'button[type="submit"]');
    const currentUrl = await t.eval(() => window.location.href);
    await t.expect(currentUrl).contains('list?success');  // Verifica che l'URL contenga 'list?success'

    await navigateTo(t, `details?id=${movieId}`);

    // Seleziona il paragrafo che contiene il testo "Title:" all'interno di .movie-details
    const titleElement = Selector('.movie-details p').withText('Title:');
    // Verifica che lo span all'interno contenga il nuovo titolo
    await t.expect(titleElement.find('span').innerText).contains('Film Aggiornato con PATCH');
});


test('Test Patch Movie Client Validation', async t => {
    await navigateTo(t, `patch?id=${movieId}`);
    const updateField = findElement('#updateValue');
    await t.selectText(updateField).pressKey('delete');  // Svuota il campo di aggiornamento
    await clickButton(t, 'button[type="submit"]');
    const currentUrl = await t.eval(() => window.location.href);
    await t.expect(currentUrl).contains(`patch?id=${movieId}`);  // Verifica che l'URL contenga l'ID del film
});

test('Test Patch Movie Not Found', async t => {
    await navigateTo(t, 'patch?id=999');
    const errorElement = findElement('.error');
    await t.expect(errorElement.exists).ok();  // Verifica che l'errore sia visibile
});

test('Test Delete Movie', async t => {
    await navigateTo(t, `delete?id=${movieId}`);
    const currentUrl = await t.eval(() => window.location.href);
    await t.expect(currentUrl).contains('list?success');
});
