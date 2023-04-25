require: scripts.js

require: dicts/answers.yaml
  var = $Answers

require: common.js
  module = sys.zb-common

require: dicts/libSound.yaml
    name = libSound
    var = $libSound
  
init:
    bind("onAnyError", function($context) {
        $reactions.answer($Answers["Error"]);
    });
    
    if (!$global.$converters){
        $global.$converters = {};
    }
    $global.$converters
        .libSoundConverter = function(parseTree){
            var id = parseTree.libSound[0].value;
            return $libSound[id].value;
        }

theme: /

    state: Start
        q!: * *start
        q!: * {(как*/шаблон*) (звук*) * [есть/имеется/созда*/запусти*]} *
        q!: * (~помощь/~справка/помоги*/help/хелп/~меню/може*/умее*) *
        script:
            $jsapi.startSession();
            $reactions.answer($Answers["Start"]);
        buttons:
            "Текст без разметки" -> /NoMarkupText
            "Текст с разметкой синтеза речи" -> /MarkupText
            "Звуки из библиотеки" -> /LibSound
            "Пользовательские звуки" -> /CustomSound
           
    state: NoMarkupText
        q!: * (без разметк*/текст) *
        q!: без
        q: * (повтор*/еще раз*/заново) * || fromState = /NoMarkupText, onlyThisState = true
        script: 
            $reactions.answer($Answers["NoMarkupText"]);
        buttons:
            "Повторить" -> /NoMarkupText
            "Текст с разметкой синтеза речи" -> /MarkupText
            "Звуки из библиотеки" -> /LibSound
            "Пользовательские звуки" -> /CustomSound
            
    # Подробнее о разметке синтеза речи: https://developer.sberdevices.ru/docs/ru/developer_tools/ssml/overview 
    state: MarkupText
        q!: * (с (разметк*/тегом*/тегам*)/теги/ссмл/ssml) *
        q!: с
        q: * (повтор*/еще раз*/заново) * || fromState = /MarkupText, onlyThisState = true 
        script: 
            answerWithSSML($Answers["MarkupText"]);
        buttons:
            "Повторить" -> /MarkupText
            "Текст без разметки" -> /NoMarkupText
            "Звуки из библиотеки" -> /LibSound
            "Пользовательские звуки" -> /CustomSound
            
    # Подробнее о добавлении звуков из библиотеки: https://developer.sberdevices.ru/docs/ru/platform_services/sound_library
    state: LibSound
        q!: * (коллекци*/звук* * коллекци*/стандартн* * звук*) *
        script:
            $session.currentSound = _.sample(_.values($libSound));
            $response.replies = $response.replies || [];
            $response.replies.push ({
                "type":"text",
                    "text": "Можно использовать различные звуки из коллекции. Например, «" + $session.currentSound.name + "»",
                    "ssml": "Можно использовать различные звуки из коллекции. Например, «" + $session.currentSound.name + "»" + " <audio text=" + $session.currentSound.link + "/>"
            });
        buttons:
            "Повторить" -> /LibSound/Repeat
            "Другой звук" -> /LibSound/Next
            "Текст без разметки" -> /NoMarkupText
            "Текст с разметкой синтеза речи" -> /MarkupText
            "Пользовательские звуки" -> /CustomSound
                
        state: Next
            q: * (дальше/далее/следующ*/еще/~другой) *
            script:
                $session.currentSound = _.sample(_.values($libSound));
                $response.replies = $response.replies || [];
                $response.replies.push ({
                    "type":"text",
                        "text": "Это «" + $session.currentSound.name + "»",
                        "ssml": "Это «" + $session.currentSound.name + "»" + " <audio text=" + $session.currentSound.link + "/>"
                });
            buttons:
                "Повторить" -> /LibSound/Repeat
                "Другой звук" -> /LibSound/Next
                "Текст без разметки" -> /NoMarkupText
                "Текст с разметкой синтеза речи" -> /MarkupText
                "Пользовательские звуки" -> /CustomSound
        
        state: Repeat
            q: * (повтор*/еще раз*/заново) * 
            script: 
                $response.replies = $response.replies || [];
                $response.replies.push ({
                    "type":"text",
                        "text": "Это «" + $session.currentSound.name + "»",
                        "ssml": "Это «" + $session.currentSound.name + "»" + " <audio text=" + $session.currentSound.link + "/>"
                });
            buttons:
                "Повторить" -> /LibSound/Repeat
                "Другой звук" -> /LibSound/Next
                "Текст без разметки" -> /NoMarkupText
                "Текст с разметкой синтеза речи" -> /MarkupText
                "Пользовательские звуки" -> /CustomSound
        
    # Подробнее об аудиозаписях в ответах ассистента: https://developer.sberdevices.ru/docs/ru/developer_tools/ide/JAICP_DSL/tags/reaction_tags
    state: CustomSound
        q!: * (загруженн*/хранилищ*/кастомн*/костомн*/((~свой/свои/собственн*/поставлен*) * звук*)) *
        q: * (повтор*/еще раз*/заново):repeat * || fromState = /CustomSound, onlyThisState = true
        if: $parseTree.value == "repeat"
            a: Повторяю.
            audio: https://content.sberdevices.ru/smartmarket-smide-prod/721/722/audio/cUNStrcyBZUZMp4L.wav
        else:
            a: Вот звук из хранилища:
            # Для того, чтобы использовать свой свой звук, загрузите его в раздел "контент" и вставьте сюда ссылку на него.
            audio: https://content.sberdevices.ru/smartmarket-smide-prod/721/722/audio/cUNStrcyBZUZMp4L.wav        
            a: Теперь можно попробовать загрузить другой аудиофайл в хранилище контента, вставить новую ссылку в сценарий и пересобрать бота.
        buttons:
            "Повторить"
            "Текст без разметки" -> /NoMarkupText
            "Текст с разметкой синтеза речи" -> /MarkupText
            "Звуки из библиотеки" -> /LibSound
                
    state: CatchAll
        q!: *
        event!: noMatch
        a: К сожалению я не понимаю.
        go!: /Start