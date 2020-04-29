package tech.artcoded.atriangle.api.dto;

import lombok.Getter;

public enum RdfType {
  RDFXML("RDF/XML"),
  NTRIPLES("N-TRIPLES"),
  TURTLE("TURTLE"),
  N3("N3"),
  TRIX("TRIX"),
  TRIG("TRIG"),
  NQUADS("N-QUADS"),
  JSONLD("JSON-LD"),
  RDFJSON("RDF/JSON"),
  RDFA("RDFA");

  @Getter private String value;

  RdfType(String value) {
    this.value = value;
  }
}
