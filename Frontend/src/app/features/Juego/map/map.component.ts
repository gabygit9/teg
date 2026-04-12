import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import { CommonModule } from '@angular/common';  // <== Importa esto
import { MapCountry } from '../../../core/models/interfaces/MapCountry';
import { Country } from '../../../core/enums/Country';
import { Continent } from '../../../core/enums/Continent';
import {AttackDto} from '../../../core/models/interfaces/AttackDto';
import {CountryGameDTO} from '../../../core/models/interfaces/CountryGame';
import {GameDataDTO} from '../../../core/models/interfaces/GameDataDTO';
import {ModalMessageService} from '../../../core/services/modal.service';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit {
  ngOnInit(): void {
    this.getCountryData();
  }
  @Input() myPlayerId!: number;
  @Input() attackMode: boolean = false;
  @Input() modeRegroup: boolean = false;
  @Output() selectedAttack = new EventEmitter<AttackDto>();
  @Output() onCountrySelectedToArmyEvent = new EventEmitter<CountryGameDTO>();
  @Output() onCountryRegroupSelectedOrigin = new EventEmitter<CountryGameDTO>();
  @Output() onCountryRegroupSelectedDestine = new EventEmitter<CountryGameDTO>();


  countrySelectedToArmy?: CountryGameDTO;
  originCountrySelectedToRegroup?: CountryGameDTO;
  destinationCountrySelectedToRegroup?: CountryGameDTO;

  get mapCountries(): MapCountry[] {
    return this._mapCountries;
  }

  selectedCountryAttacker?: CountryGameDTO;
  selectedCountryDefensor?: CountryGameDTO;

  constructor(private modalService: ModalMessageService) {}

  getCountryData() {
    const rawData = sessionStorage.getItem("gameData");
    if (!rawData) {
      console.warn("No se encontró 'gameData' en sessionStorage");
      return;
    }
    const gameData: GameDataDTO = JSON.parse(rawData);
    const gameCountries = gameData.countries;

    this._mapCountries.forEach( c => {
      // Buscar la coincidencia en gameCountries
      let match: CountryGameDTO | undefined;
      for (let i = 0; i < gameCountries.length; i++) {
        if (c.countryName === gameCountries[i].countryName) {
          match = gameCountries[i];
          break;
        }
      }
      if (!match) return;

      // Asignar valores (match ya no es null/undefined)
      c.playerId = match.playerId;
      c.amountArmies = match.amountArmies;
      c.countryId = match.countryId;
      c.gameId = match.gameId;
      c.playerName = match.playerName;
      c.color = match.color;
    });

  }
  getChipStyle(country: MapCountry): { [key: string]: string } {
    const baseWidth = 2250;
    const baseHeight = 1143;

    const top = (country.y + country.height / 2) / baseHeight * 100;
    const left = (country.x + country.width / 2) / baseWidth * 100;

    return {
      position: 'absolute',
      top: `${top}%`,
      left: `${left}%`,
      transform: 'translate(-50%, -50%)',
      color: 'white',
      'z-index': '20'
    };
  }

  onCountriesClick(country: CountryGameDTO) {
    if (!this.attackMode) return;

    if (!this.selectedCountryAttacker) {
      console.log("Click en país", country.countryName, "- jugadorId:", country.playerId, "cantidadEjercitos:", country.amountArmies, "miJugadorId:", this.myPlayerId);
      if (country.playerId !== this.myPlayerId || country.amountArmies <= 1) {
        // alert("Debes seleccionar un país tuyo con más de 1 ejército.");
        this.modalService.modalMessage(`Debes seleccionar un país tuyo con más de 1 ejército.`, 'Ataque');
        return;
      }
      this.selectedCountryAttacker = country;
      return;
    }

    if (!this.selectedCountryDefensor) {
      if (country.countryId === this.selectedCountryAttacker.countryId) return;
      if (country.playerId === this.myPlayerId) {
        // alert("No puedes atacar a tus propios países.");
        this.modalService.modalMessage(`No puedes atacar a tus propios países.`, 'Ataque');

        return;
      }

      // Emitir el ataque
      console.log("Ejércitos del atacante:", this.selectedCountryAttacker.amountArmies);

      const dto: AttackDto = {
        gameId: country.gameId,
        countryIdAttacker: this.selectedCountryAttacker.countryId,
        countryIdDefensor: country.countryId,
        dice: this.calculateAttackerDice(this.selectedCountryAttacker.amountArmies!)

      };
      if (dto.dice === 0) {
        // alert("No puedes atacar con solo 1 ejército.");
        this.modalService.modalMessage(`No puedes atacar con solo 1 ejército.`, 'Ataque');
        return;
      }

      this.selectedAttack.emit(dto);

      // Reset selección
      this.selectedCountryAttacker = undefined;
      this.selectedCountryDefensor = undefined;
    }
  }

  private calculateAttackerDice(armies: number): number {
    if (armies <= 1) return 0;
    if (armies === 2) return 1;
    if (armies === 3) return 2;
    return 3;
  }

  route: string = 'assets/SVGimages/';

  colorFilters: Record<string, string> = {
    'red': 'brightness(0.2) sepia(1) hue-rotate(0deg) saturate(3000%)',
    'blue': 'brightness(0.4) sepia(1) hue-rotate(200deg) saturate(3000%)',
    'yellow': 'brightness(1) sepia(1) hue-rotate(0deg) saturate(3000%)',
    'green': 'brightness(0.4) sepia(1) hue-rotate(100deg) saturate(300%)',
    'black': 'brightness(0.15) sepia(1) saturate(0%)',
    'magenta': 'brightness(0.4) sepia(1) hue-rotate(235deg) saturate(3000%)'
  };

  getSvgColorFilter(colorName: string): string {
    return this.colorFilters[colorName.toLowerCase()] || 'none';
  }
  @Input() set updatedCountries(countries: CountryGameDTO[]) {
    this.mapCountries.forEach(country => {
      const updatedCountry = countries.find(p => p.countryId === country.countryId);
      if (updatedCountry) {
        country.playerId = updatedCountry.playerId;
        country.amountArmies = updatedCountry.amountArmies;
        country.color = updatedCountry.color;
        country.playerName = updatedCountry.playerName;
      }
    });
  }


  _mapCountries : MapCountry[] = [
    //#region South America
    {
      countryName: Country.CHILI,
      continent: Continent.SOUTH_AMERICA,
      svgPath: this.route + 'America_del_Sur_Chile.svg',
      x: 800,
      y: 730,
      width: 52,
      height: 278,
      rotate: 0,
      color: "red",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.ARGENTINE,
      continent: Continent.SOUTH_AMERICA,
      svgPath: this.route + 'America_del_Sur_Argentina.svg',
      x: 820,
      y: 735,
      width: 115,
      height: 276,
      rotate: 0,
      color: "blue",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.URUGUAY,
      continent: Continent.SOUTH_AMERICA,
      svgPath: this.route + 'America_del_Sur_Uruguay.svg',
      x: 905,
      y: 760,
      width: 100,
      height: 126,
      rotate: 0,
      color: "yellow",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.PERU,
      continent: Continent.SOUTH_AMERICA,
      svgPath: this.route + 'America_del_Sur_Peru.svg',
      x: 781,
      y: 675,
      width: 120,
      height: 142,
      rotate: 0,
      color: "green",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.BRAZIL,
      continent: Continent.SOUTH_AMERICA,
      svgPath: this.route + 'America_del_Sur_Brasil.svg',
      x: 860,
      y: 590,
      width: 220,
      height: 247,
      rotate: 2,
      color: "black",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.COLOMBIA,
      continent: Continent.SOUTH_AMERICA,
      svgPath: this.route + 'America_del_Sur_Colombia.svg',
      x: 755,
      y: 600,
      width: 140,
      height: 156,
      rotate: -8,
      color: "magenta",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    //#endregion
    //#region North America
    {
      countryName: Country.MEXICO,
      continent: Continent.NORTH_AMERICA,
      svgPath: this.route + 'America_del_Norte_Mexico.svg',
      x: 595,
      y: 500,
      width: 192,
      height: 202,
      rotate: -1,
      color: "green",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.OREGON,
      continent: Continent.NORTH_AMERICA,
      svgPath: this.route + 'America_del_Norte_Orgeon.svg',
      x: 370,
      y: 385,
      width: 258,
      height: 277,
      rotate: 1,
      color: "green",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.NEW_YORK,
      continent: Continent.NORTH_AMERICA,
      svgPath: this.route + 'America_del_Norte_Nueva_York.svg',
      x: 576,
      y: 320,
      width: 225,
      height: 247,
      rotate: -1,
      color: "yellow",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.CANADA,
      continent: Continent.NORTH_AMERICA,
      svgPath: this.route + 'America_del_Norte_Canada.svg',
      x: 510,
      y: 125,
      width: 196,
      height: 364,
      rotate: -1,
      color: "red",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.ALASKA,
      continent: Continent.NORTH_AMERICA,
      svgPath: this.route + 'America_del_Norte_Alaska.svg',
      x: 357,
      y: 300,
      width: 95,
      height: 296,
      rotate: 0,
      color: "black",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.CALIFORNIA,
      continent: Continent.NORTH_AMERICA,
      svgPath: this.route + 'America_del_Norte_California.svg',
      x: 495,
      y: 445,
      width: 220,
      height: 246,
      rotate: 0,
      color: "blue",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.YUKON,
      continent: Continent.NORTH_AMERICA,
      svgPath: this.route + 'America_del_Norte_Yukon.svg',
      x: 400,
      y: 191,
      width: 169,
      height: 336,
      rotate: 1,
      color: "magenta",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.TERRANOVA,
      continent: Continent.NORTH_AMERICA,
      svgPath: this.route + 'America_del_Norte_Terranova.svg',
      x: 620,
      y: 280,
      width: 180,
      height: 230,
      rotate: 0,
      color: "black",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.LABRADOR,
      continent: Continent.NORTH_AMERICA,
      svgPath: this.route + 'America_del_Norte_Labrador.svg',
      x: 705,
      y: 276,
      width: 112,
      height: 158,
      rotate: 0,
      color: "green",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.GREENLAND,
      continent: Continent.NORTH_AMERICA,
      svgPath: this.route + 'America_del_Norte_Groenlandia.svg',
      x: 760,
      y: 130,
      width: 203,
      height: 340,
      rotate: 0,
      color: "blue",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    }
    //#endregion
    //#region Europe
    ,
    {
      countryName: Country.ICELAND,
      continent: Continent.EUROPE,
      svgPath: this.route + 'Europe_Islandia.svg',
      x: 970,
      y: 350,
      width: 126,
      height: 214,
      rotate: 0,
      color: "green",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.RUSSIA,
      continent: Continent.EUROPE,
      svgPath: this.route + 'Europe_Rusia.svg',
      x: 1345,
      y: 180,
      width: 240,
      height: 441,
      rotate: 0,
      color: "blue",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.GREAT_BRITAIN,
      continent: Continent.EUROPE,
      svgPath: this.route + 'Europe_Gran_Bretana.svg',
      x: 1125,
      y: 360,
      width: 122,
      height: 217,
      rotate: 0,
      color: "yellow",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.SPAIN,
      continent: Continent.EUROPE,
      svgPath: this.route + 'Europe_Espania.svg',
      x: 1105,
      y: 565,
      width: 135,
      height: 185,
      rotate: 0,
      color: "red",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.FRANCE,
      continent: Continent.EUROPE,
      svgPath: this.route + 'Europe_Francia.svg',
      x: 1200,
      y: 470,
      width: 128,
      height: 213,
      rotate: 0,
      color: "blue",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.ITALY,
      continent: Continent.EUROPE,
      svgPath: this.route + 'Europe_Italia.svg',
      x: 1300,
      y: 560,
      width: 135,
      height: 210,
      rotate: 0,
      color: "yellow",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.POLAND,
      continent: Continent.EUROPE,
      svgPath: this.route + 'Europe_Polonia.svg',
      x: 1340,
      y: 410,
      width: 160,
      height: 238,
      rotate: 0,
      color: "green",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.GERMANY,
      continent: Continent.EUROPE,
      svgPath: this.route + 'Europe_Alemania.svg',
      x: 1295,
      y: 425,
      width: 140,
      height: 246,
      rotate: 0,
      color: "black",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.SWEDEN,
      continent: Continent.EUROPE,
      svgPath: this.route + 'Europe_Suecia.svg',
      x: 1245,
      y: 225,
      width: 120,
      height: 195,
      rotate: 0,
      color: "red",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    }
    //#endregion
    //#region Asia
    ,
    {
      countryName: Country.CHINA,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_China.svg',
      x: 1650,
      y: 225,
      width: 250,
      height: 402,
      rotate: 1,
      color: "red",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.INDIA,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_India.svg',
      x: 1665,
      y: 505,
      width: 120,
      height: 244,
      rotate: 0,
      color: "yellow",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.ARABIA,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_Arabia.svg',
      x: 1530,
      y: 580,
      width: 120,
      height: 162,
      rotate: 0,
      color: "yellow",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.ARAL,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_Aral.svg',
      x: 1440,
      y: 175,
      width: 100,
      height: 225,
      rotate: 0,
      color: "yellow",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.IRAN,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_Iran.svg',
      x: 1515,
      y: 310,
      width: 165,
      height: 325,
      rotate: 1,
      color: "magenta",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.ISRAEL,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_Israel.svg',
      x: 1480,
      y: 570,
      width: 125,
      height: 137,
      rotate: 2,
      color: "green",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.JAPAN,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_Japon.svg',
      x: 1760,
      y: 220,
      width: 140,
      height: 180,
      rotate: 1,
      color: "white",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.KAMTCHATKA,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_Kamtchatka.svg',
      x: 1655,
      y: 180,
      width: 110,
      height: 143,
      rotate: -1,
      color: "yellow",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.MALAYSIA,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_Malasia.svg',
      x: 1765,
      y: 475,
      width: 120,
      height: 187,
      rotate: 8,
      color: "magenta",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.MONGOLIA,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_Mongolia.svg',
      x: 1530,
      y: 292,
      width: 165,
      height: 193,
      rotate: 0,
      color: "",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.SIBERIA,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_Siberia.svg',
      x: 1525,
      y: 205,
      width: 175,
      height: 163,
      rotate: -1,
      color: "black",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.TAIMIR,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_Tamyr.svg',
      x: 1560,
      y: 175,
      width: 90,
      height: 147,
      rotate: 1,
      color: "red",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.TARTARIA,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_Tartaria.svg',
      x: 1490,
      y: 150,
      width: 95,
      height: 184,
      rotate: -1,
      color: "blue",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.GOBI,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_Gobi.svg',
      x: 1605,
      y: 355,
      width: 112,
      height: 220,
      rotate: 1,
      color: "black",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.TURKEY,
      continent: Continent.ASIA,
      svgPath: this.route + 'Asia_Turquia.svg',
      x: 1440,
      y: 493,
      width: 230,
      height: 168,
      rotate: 4,
      color: "black",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    }
    //#endregion
    //#region Oceania
    ,
    {
      countryName: Country.AUSTRALIA,
      continent: Continent.OCEANIA,
      svgPath: this.route + 'Oceania_Australia.svg',
      x: 1710,
      y: 710,
      width: 190,
      height: 273,
      rotate: 0,
      color: "black",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.SUMATRA,
      continent: Continent.OCEANIA,
      svgPath: this.route + 'Oceania_Sumatra.svg',
      x: 1640,
      y: 690,
      width: 90,
      height: 150,
      rotate: 0,
      color: "red",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.BORNEO,
      continent: Continent.OCEANIA,
      svgPath: this.route + 'Oceania_Borneo.svg',
      x: 1755,
      y: 595,
      width: 70,
      height: 168,
      rotate: 0,
      color: "blue",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.JAVA,
      continent: Continent.OCEANIA,
      svgPath: this.route + 'Oceania_Java.svg',
      x: 1830,
      y: 590,
      width: 70,
      height: 179,
      rotate: 0,
      color: "green",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    }
    //#endregion
    //#region Africa
    ,
    {
      countryName: Country.SAHARA,
      continent: Continent.AFRICA,
      svgPath: this.route + 'Africa_Sahara.svg',
      x: 1223,
      y: 703,
      width: 177,
      height: 216,
      rotate: 0,
      color: "blue",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.EGYPT,
      continent: Continent.AFRICA,
      svgPath: this.route + 'Africa_Egipto.svg',
      x: 1370,
      y: 695,
      width: 220,
      height: 158,
      rotate: 0,
      color: "magenta",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.MADAGASCAR,
      continent: Continent.AFRICA,
      svgPath: this.route + 'Africa_Madagascar.svg',
      x: 1558,
      y: 786,
      width: 85,
      height: 204,
      rotate: 0,
      color: "yellow",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.SOUTH_AFRICA,
      continent: Continent.AFRICA,
      svgPath: this.route + 'Africa_Sudafrica.svg',
      x: 1410,
      y: 832,
      width: 120,
      height: 190,
      rotate: 0,
      color: "green",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.ETHIOPIA,
      continent: Continent.AFRICA,
      svgPath: this.route + 'Africa_Etiopia.svg',
      x: 1360,
      y: 760,
      width: 185,
      height: 130,
      rotate: 0,
      color: "red",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    },
    {
      countryName: Country.ZAIRE,
      continent: Continent.AFRICA,
      svgPath: this.route + 'Africa_Zaire.svg',
      x: 1300,
      y: 794,
      width: 155,
      height: 165,
      rotate: 0,
      color: "yellow",
      countryId: 0,
      gameId: 0,
      amountArmies: 0,
      playerId: 0,
      playerName: ''
    }
    //#endregion
  ];


  checkPixelTransparency(event: MouseEvent, pais: MapCountry): void {
    const img = event.target as HTMLImageElement;
    const rect = img.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');

    if (!ctx) return;

    if (img.complete) {
      this.checkPixel(ctx, canvas, img, x, y, pais);
    } else {
      img.onload = () => {
        this.checkPixel(ctx, canvas, img, x, y, pais);
      };
    }
  }

  checkPixel(ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement,
                     img: HTMLImageElement, x: number, y: number, pais: MapCountry): void {
    canvas.width = img.naturalWidth || img.width;
    canvas.height = img.naturalHeight || img.height;

    ctx.drawImage(img, 0, 0);

    const scaleX = canvas.width / img.width;
    const scaleY = canvas.height / img.height;

    const pixelData = ctx.getImageData(x * scaleX, y * scaleY, 1, 1);
    const alpha = pixelData.data[3];

    pais.hovered = alpha > 50;
  }

  onCountryClick(country: CountryGameDTO): void {
    if (this.attackMode) {
      this.onCountriesClick(country);

    } else if (this.modeRegroup) {
      if (!this.originCountrySelectedToRegroup) {
        if (country.playerId !== this.myPlayerId || country.amountArmies! <= 1) {
          // alert("Debes seleccionar un país tuyo con más de 1 ejército como origen.");
          this.modalService.modalMessage(`Debes seleccionar un país de origen con más de 1 ejército.`, 'Ataque');
          return;
        }

        this.originCountrySelectedToRegroup = country;
        this.onCountryRegroupSelectedOrigin.emit(country);
        this.markSelection(country.countryId, 'origin');

        console.log('País origen para reagrupar:', country);

      } else if (!this.destinationCountrySelectedToRegroup) {
        if (country.countryId === this.originCountrySelectedToRegroup.countryId) {
          this.deselectCountries();
          return;
        }

        if (country.playerId !== this.myPlayerId) {
          // alert("Solo puedes reagrupar entre tus propios países.");
          this.modalService.modalMessage(`Solo puedes reagrupar entre tus propios países.`, 'Reagrupar');

          return;
        }

        this.destinationCountrySelectedToRegroup = country;
        this.onCountryRegroupSelectedDestine.emit(country);
        this.markSelection(country.countryId, 'destine');

        console.log('País destino para reagrupar:', country);

        this.originCountrySelectedToRegroup = undefined;
        this.destinationCountrySelectedToRegroup = undefined;
      }

    } else {
      if (this.countrySelectedToArmy?.countryId === country.countryId) {
        this.deselectCountries();
      } else {
        this.countrySelectedToArmy = country;
        this.onCountrySelectedToArmyEvent.emit(country);
        this.markSelection(country.countryId, 'army');

        console.log('País seleccionado para ejercito:', country);
      }
    }
  }

  deselectCountries(): void {
    this.originCountrySelectedToRegroup = undefined;
    this.destinationCountrySelectedToRegroup = undefined;
    this.countrySelectedToArmy = undefined;
    this.selectedCountryAttacker = undefined;
    this.selectedCountryDefensor = undefined;

    this.resetVisualSelection();
  }

  private resetVisualSelection(): void {
    for (let i = 0; i < this.mapCountries.length; i++) {
      const c = this.mapCountries[i];
      c.isSelectedOrigin = false;
      c.isSelectedDestine = false;
      c.isSelectedArmy = false;
    }
  }

  private markSelection(countryId: number, type: 'origin' | 'destine' | 'army'): void {
    this.resetVisualSelection();

    for (let i = 0; i < this.mapCountries.length; i++) {
      const c = this.mapCountries[i];
      if (c.countryId === countryId) {
        if (type === 'origin') c.isSelectedOrigin = true;
        else if (type === 'destine') c.isSelectedDestine = true;
        else if (type === 'army') c.isSelectedArmy = true;
      }
    }
  }


  getCountryStyle(country: MapCountry): any {

    const baseWidth = 2250;
    const baseHeight = 1143;

    return {
      top: (country.y / baseHeight * 100) + '%',
      left: (country.x / baseWidth * 100) + '%',
      width: (country.width / baseWidth * 100) + '%',
      height: (country.height / baseHeight * 100) + '%',
      transform: `rotate(${country.rotate}deg)`,
      filter: this.getSvgColorFilter(country.color),
    };
  }

}
