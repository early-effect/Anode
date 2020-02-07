package worker

trait Context {
  def name: String
  def version: String
  def scalaVersion: String
  def sbtVersion: String
  def cachedAssets: scala.collection.Seq[String]
}
