exec { "apt-get update":
  command => "/usr/bin/apt-get update",
}

package { "tomcat7":
  ensure  => "installed",
  require => exec [ "apt-get update" ],
}
