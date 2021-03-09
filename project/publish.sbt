
credentials += Credentials(
  realm = "GnuPG Key ID",
  host = "gpg",
  userName = "216F04130F31F75E00AA1B5F4060B67C18B9B8C3", // key identifier
  passwd = "ignored",                                    // this field is ignored; passwords are supplied by pinentry
)

credentials += Credentials(
  realm = "Sonatype Nexus Repository Manager",
  host = "s01.oss.sonatype.org",
  userName = sys.env.getOrElse("SONATYPE_USERNAME", "BAD"),
  passwd = sys.env.getOrElse("SONATYPE_PASSWORD", "BAD"),
)

