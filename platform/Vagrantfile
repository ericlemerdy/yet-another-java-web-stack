VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "base"
  config.vm.network :private_network, ip: "10.10.10.2"
  config.vm.provision "puppet" do |puppet|
    puppet.options = "--verbose --debug"
  end
end
