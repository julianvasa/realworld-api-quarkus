package org.example.realworldapi.domain.repository;

import org.example.realworldapi.domain.entity.persistent.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository {
  Optional<Tag> findByName(String tagName);

  Tag create(Tag tag);

  List<Tag> findAll();
}
