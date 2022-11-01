package posting.logic

import org.http4s.Uri
import org.http4s.Uri.Path
import posting.logic.PostConfig.{Reader, Storage}

private[posting] final case class PostConfig(storage: Storage, reader: Reader)
private[posting] object PostConfig {
  final case class Storage(directory: String, maxConcurrentStores: Int)
  final case class Reader(uri: Uri, postPath: Path, commentPath: Path)
}
