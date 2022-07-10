package Server.utils;
public enum ResultCode {
   OK(0, "Operazione completata."), //da utilizzare quando l'operazione viene semplicemente completata, senza bisogno di altre spiegazioni
   USER_ALREADY_REGISTERED(1, "Errore!: L'utente è già registrato!"),
   USER_NOT_FOUND(2, "Errore!: L'utente non è stato trovato!"),
   ILLEGAL_OPERATION(3, "Errore!: Operazione non valida!"),
   MISSING_OPERAND(4, "Errore!: Manca almeno un operando per questa richiesta!"),
   USER_NOT_LOGGED(5, "Errore!: Devi ancora effettuare il login!"),
   RATE_OWN_POST(6, "Errore!: Non si può votare un proprio post!"),
   ALREADY_RATED(7, "Errore!: Hai già votato questo post!"),
   RATE_BEFORE_REWIN(8, "Errore!: Prima di votare questo post, devi effettuare il rewin!"),
   MALFORMED_INPUT(9, "Errore!: Il comando richiesto è mal costruito!"),
   POST_NOT_FOUND(10, "Errore!: Il post richiesto non è stato trovato!"),
   UNAUTHORIZED_USER(11, "Errore!: Non sei autorizzato a svolgere questa operazione!"),
   NO_EFFECT(12, "Errore!: L'azione che hai fatto non ha cambiato nulla nel Server..."),
   ANSWER_MSG(13, "Arriva una risposta!");
   


   private final int code;
   private final String description;

   private ResultCode(int code, String description) {
      this.code = code;
      this.description = description;
    }
  
    public String getDescription() {
       return description;
    }
  
    public int getCode() {
       return code;
    }
  
    @Override
    public String toString() {
      return description + " (codice di uscita: " +code + ")";
    }
  }

