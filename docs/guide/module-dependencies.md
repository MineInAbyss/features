# Inter-module dependencies

We often need modules to depend on injected values from other modules. While it's possible to manually share DI instances between modules, we also want to correctly be able to reload a module's dependants when it gets reloaded.

# submodule

# singleModule

`singleModule` acts like calling `scope.load(ParentModule)`, but ensures that when `ParentModule` gets reloaded in `scope`, this module will be reloaded too.
