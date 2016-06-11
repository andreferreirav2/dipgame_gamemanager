# GameManager - An Alternative GameManager for DipGame

A simpler GameManager to launch multiple executions of a DipGame simulation and collect their results, without a GUI.

This project was initially created to help collect the results for the DipBlue, a DipGame bot created during my Masters Thesis and located at [andreferreirav2/dipblue](https://github.com/andreferreirav2/dipblue).

## Installation

The installation of this project is the same as [the original GameManager](http://www.dipgame.org/browse/gameManager) built by the Angela Fabregues and the other developers/contributors of the [DipGame](http://www.dipgame.org/).

## Usage

To run the simulations all you need to do is run the `main()` method of the `GameLauncher`. This will, according to the example below, run 70 iterations of the game using 2 DipBlue bots and the remaining 5 DumbBots.

```java

gameBatch(70, Archetype.DIPBLUE, Archetype.DIPBLUE);
```

The source code of the DipBlue Bot must be included in the path of the execution. 

## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

## Credits

* [André Ferreira](https://github.com/andreferreirav2)
*  [Henrique Lopes Cardoso (FEUP)](https://up-pt.academia.edu/HenriqueCardoso) and [Luis Paulo Reis (UMinho)](http://uminho.academia.edu/LuisPauloReis)
* [Angela Fabregues and the other developers/contributors of the DipGame](http://www.dipgame.org/).

## License

MIT: http://rem.mit-license.org