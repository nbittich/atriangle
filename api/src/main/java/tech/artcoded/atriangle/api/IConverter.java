package tech.artcoded.atriangle.api;

public interface IConverter<I, O> {
    O apply(I input);
}
