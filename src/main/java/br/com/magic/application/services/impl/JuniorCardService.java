package br.com.magic.application.services.impl;

import br.com.magic.application.commons.MagicErrorCode;
import br.com.magic.application.entity.dto.JuniorCardDTO;
import br.com.magic.application.entity.dto.PlayerDTO;
import br.com.magic.application.entity.mapper.JuniorCardMapper;
import br.com.magic.application.entity.mapper.PlayerMapper;
import br.com.magic.application.entity.model.JuniorCard;
import br.com.magic.application.entity.model.Player;
import br.com.magic.application.exception.CardNotFound;
import br.com.magic.application.exception.PlayerFullCards;
import br.com.magic.application.repositories.JuniorCardRepositorie;
import br.com.magic.application.services.IJuniorCardService;
import br.com.magic.application.services.IPlayerService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JuniorCardService implements IJuniorCardService {

    private final JuniorCardRepositorie juniorCardRepositorie;
    private final IPlayerService playerService;
    private final JuniorCardMapper juniorCardMapper;
    private final PlayerMapper playerMapper;

    @Autowired
    public JuniorCardService(JuniorCardRepositorie juniorCardRepositorie, IPlayerService playerService, JuniorCardMapper juniorCardMapper,
                             PlayerMapper playerMapper) {
        this.juniorCardRepositorie = juniorCardRepositorie;
        this.playerService = playerService;
        this.juniorCardMapper = juniorCardMapper;
        this.playerMapper = playerMapper;
    }

    @Override
    public List<JuniorCardDTO> getCards() {
        List<JuniorCard> juniorCards = juniorCardRepositorie.findAll();

        return juniorCardMapper.toDto(juniorCards);
    }

    @Override
    public List<JuniorCardDTO> getCardsWithoutPlayer() {
        List<JuniorCard> juniorCards = juniorCardRepositorie.findAllByPlayerIsNullOrderById();

        return juniorCardMapper.toDto(juniorCards);
    }

    @Override
    public JuniorCardDTO findById(Long id) {
        JuniorCard juniorCard = juniorCardRepositorie.findById(id).orElseThrow(() -> new CardNotFound(MagicErrorCode.MEC004));

        return juniorCardMapper.toDto(juniorCard);
    }

    @Override
    public JuniorCardDTO findByPlayerId(Long playerId) {
        JuniorCard juniorCard = juniorCardRepositorie.findByPlayerId(playerId).orElseThrow(() -> new CardNotFound(MagicErrorCode.MEC004));

        return juniorCardMapper.toDto(juniorCard);
    }

    @Override
    public void saveCardsIntoPlayer(List<JuniorCardDTO> cardsDto, Long id) {
        List<JuniorCard> cards = juniorCardMapper.toEntity(cardsDto);
        PlayerDTO playerDTO = playerService.findById(id);
        Player player = playerMapper.toEntity(playerDTO);

        List<JuniorCard> cardsWithUser = juniorCardRepositorie.findAllByPlayer(player);

        if (cardsWithUser.size() == 4) {
            throw new PlayerFullCards(MagicErrorCode.MEC002);
        }

        cards.forEach(juniorCard -> {
            juniorCard.setPlayer(player);
        });

        juniorCardRepositorie.saveAll(cards);
    }

    @Override
    public void removeCardFromJunior(JuniorCardDTO juniorCardDTO) {
        JuniorCard juniorCard = juniorCardMapper.toEntity(juniorCardDTO);
        juniorCard.setPlayer(null);

        juniorCardRepositorie.save(juniorCard);
    }


}
