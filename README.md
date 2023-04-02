### Features

* Import from Excel typical cost estimation Excel templates (allow bold headers/two level hierarchy)
* Import from Excel HSE type plan + cost estimation template (plan allows deep hierarch)
* Change value cells + recalculation
* Change items structure/hierarchy + recalculation
* Change MoneyPerHours, risks 
* Copy/Paste to Excel (Ctrl+A works in a table)

### TODO

* Project Cost versioning https://stackoverflow.com/questions/4185105/ways-to-implement-data-versioning-in-mongodb
* Add working with JSON Patch https://www.rfc-editor.org/rfc/rfc6902
  https://stackoverflow.com/a/57511258
* Rewrite API to support one level items in storage
* ~~Color and comments~~
* ~~Export/Import to Excel Costs (High Priority)~~
* ~~Import from Excel Plan (High Priority)~~
* Undo/Redo
* Async push calculation to FrontEnd (Possible use JSON Patch)
* ~~Make OrderingService as entry point, remove a move methods from CostItemService~~
* ~~Make reordering like in a one transaction~~

### Importer

* ~~Add import for common cost rows (cost in hour, cost in hour (money - without nds), cost in hour (money - with nds))~~
* ~~Add multiplicator-per type (dev, qa)~~
* ~~Add money per hour / recalculate in service~~
* ~~Too many numbers after comma, reduce during import~~
* ~~Add API to upload file~~
* ~~Do not calculate graph during import~~
* Enable cluster/transaction make import to slow

### UI Features

* Implement collapse all/expand all
* Do not expand all elements after changing cell value
* Add Import/Export from/to Excel
* Make switch button to hide/show empty columns
* Add local for numbers
* Add spaces for money to slit digitals like 1_000_000
* Use comma instead of dot for floating numbers

### UI Bugs

* Fix reorder logic with saving level element
* Fix styles for context menu

### Bugs

* Некорректно работает goLevelDown в случае когда у элемента под который
  мы хотим отправить элемент уже имеются дочерние элементы
* Некорректно работает перемещение нескольких элементов
* ~~В отчет не выводятся риски~~

### Feature

* Удобное формирование плана непосредственно в UI, без использования Excel (параллельная работа)
* Suggestion graph. Система должна подсказывать пользователю в ситуации, когда вычисляемое значение заменено
  на вручную проставленное. Пользователь может либо оставить вручную проставленное значение, либо согласиться
  с использованием предложенного вычисленного значения
* В любой момент пользователь должен иметь возможность отказаться от вручную проставленного значения
  и перейти на авто-вычисляемое (актуально для показателей расчитываемых относительно базового dev)
* Поддержка "распыления" некоторого объема работ в существующую оценку (в оценку с риском, до округления).
* Фиксация версии оценки с возможностью ее дальнейшего просмотра, а также просмотра разницы между двумя выбранными
  оценками
* Возможность приложить исходные файлы/объекты, так или иначе влияющие на оценку
* Контроль стоимости проекта с учетом выбранной команды


### How to build

# To resolve problem of chicken/egg execute
docker build -t project-planning/builder -f deployment/builder/builder-base-image.dockerfile .
