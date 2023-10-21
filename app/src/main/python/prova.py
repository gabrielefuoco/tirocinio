from sklearn.datasets import load_boston
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression
from sklearn.metrics import mean_absolute_error

def calcola_errore_medio_assoluto():
    # load dataset
    dataset = load_boston()
    # assegnamento dati alle variabili X e y
    X = dataset['data']
    y = dataset['target']
    # divisione tra training set e test set
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3, random_state=0)
    # instanziazione LinearRegression
    modello = LinearRegression()
    # training modello
    modello.fit(X_train, y_train)
    # previsione
    p = modello.predict(X_test)
    # calcoloerrore medio assoluto
    m = mean_absolute_error(y_test, p)

    return m

#print
errore_medio = calcola_errore_medio_assoluto()
print(f"L'errore medio assoluto è: {errore_medio}")