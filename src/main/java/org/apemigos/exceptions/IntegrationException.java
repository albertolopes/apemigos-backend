package org.apemigos.exceptions;

public class IntegrationException extends RuntimeException {

    private static final long serialVersionUID = 1L;


    public IntegrationException() {
        super();
    }

    public IntegrationException(Throwable cause) {
        super(cause);
    }

    public IntegrationException(String mensagem) {
        super(mensagem);
    }

    public IntegrationException(String mensagem, Throwable cause) {
        super(mensagem, cause);

    }
}
