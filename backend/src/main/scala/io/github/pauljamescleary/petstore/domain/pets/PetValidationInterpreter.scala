package io.github.pauljamescleary.petstore.domain.pets

import cats._
import cats.data.EitherT
import cats.implicits._
import io.github.pauljamescleary.petstore.domain.{PetAlreadyExistsError, PetNotFoundError}
import io.github.pauljamescleary.petstore.shared.domain.pets.Pet

class PetValidationInterpreter[F[_]: Monad](repository: PetRepositoryAlgebra[F])
    extends PetValidationAlgebra[F] {
  
  def doesNotExist(pet: Pet): EitherT[F, PetAlreadyExistsError, Unit] = EitherT {
    repository.findByNameAndCategory(pet.name, pet.category).map { matches =>
      if (matches.forall(possibleMatch => possibleMatch.bio != pet.bio)) {
        Right(())
      } else {
        Left(PetAlreadyExistsError(pet))
      }
    }
  }

  def exists(petId: Option[Long]): EitherT[F, PetNotFoundError.type, Unit] =
    EitherT {
      petId match {
        case Some(id) =>
          // Ensure is a little tough to follow, it says "make sure this condition is true, otherwise throw the error specified
          // In this example, we make sure that the option returned has a value, otherwise the pet was not found
          repository.get(id).map {
            case Some(_) => Right(())
            case _ => Left(PetNotFoundError)
          }
        case _ =>
          Either.left[PetNotFoundError.type, Unit](PetNotFoundError).pure[F]
      }
    }
}

object PetValidationInterpreter {
  def apply[F[_]: Monad](repository: PetRepositoryAlgebra[F]) =
    new PetValidationInterpreter[F](repository)
}