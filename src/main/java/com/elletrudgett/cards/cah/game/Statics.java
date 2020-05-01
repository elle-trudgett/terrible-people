package com.elletrudgett.cards.cah.game;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;

import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Optional;

/**
 * Helper class to be able to get objects in the current context for
 * the current player.
 */
public class Statics {
    public static final String TERRIBLE_PEOPLE_NAME = "terrible-people-name";
    public static final String TERRIBLE_PEOPLE_EMAIL = "terrible-people-email";

    private static final String SESSION_KEY_PLAYER = "player";
    private static final String SESSION_KEY_ROOM = "room";

    private static ThreadLocal<WrappedSession> currentSession = new ThreadLocal<>();

    public static void establishSession(WrappedSession wrappedSession) {
        currentSession.set(wrappedSession);
    }

    private static <T> Optional<T> getSessionObject(String key, Class<T> clazz) {
        VaadinSession currentVaadinSession = VaadinSession.getCurrent();

        WrappedSession wrappedSession = currentSession.get();
        if (currentVaadinSession != null) {
            wrappedSession = currentVaadinSession.getSession();
            currentSession.set(wrappedSession);
        }

        Object value = wrappedSession.getAttribute(key);
        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(clazz.cast(value));
    }

    private static <T> void saveSessionObject(UI ui, String key, T object) {
        if (VaadinSession.getCurrent() != null) {
            VaadinSession.getCurrent().getSession().setAttribute(key, object);
        } else if (currentSession.get() != null) {
            currentSession.get().setAttribute(key, object);
        } else {
            throw new UnsupportedOperationException("Shouldn't need to pass ui");
            //ui.access(() -> VaadinSession.getCurrent().getSession().setAttribute(key, object));
        }
    }

    public static boolean hasPlayer() {
        return getPlayerOptional().isPresent();
    }

    public static Optional<Player> getPlayerOptional() {
        return getSessionObject(SESSION_KEY_PLAYER, Player.class);
    }

    public static Player getPlayer() {
        return getPlayerOptional().orElseThrow(() -> new IllegalStateException("No player object to get."));
    }

    public static void setPlayer(UI ui, Player player) {
        saveSessionObject(ui, SESSION_KEY_PLAYER, player);
    }

    public static void unsetPlayer(UI ui) {
        setPlayer(ui, null);
    }

    public static Optional<Room> getRoomOptional() {
        return getSessionObject(SESSION_KEY_ROOM, Room.class);
    }

    public static Room getRoom() {
        return getRoomOptional().orElseThrow(() -> new IllegalStateException("No room object to get."));
    }

    public static void setRoom(UI ui, Room room) {
        saveSessionObject(ui, SESSION_KEY_ROOM, room);
    }

    public static void unsetRoom(UI ui) {
        setRoom(ui, null);
    }

    public static void saveUser(User user) throws UnsupportedEncodingException {
        // Save cookie
        Cookie nameCookie = new Cookie(TERRIBLE_PEOPLE_NAME, URLEncoder.encode(user.getName(), "UTF-8"));
        nameCookie.setMaxAge(31556952);
        nameCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
        VaadinService.getCurrentResponse().addCookie(nameCookie);

        Cookie emailCookie = new Cookie(TERRIBLE_PEOPLE_EMAIL, URLEncoder.encode(user.getEmail(), "UTF-8"));
        emailCookie.setMaxAge(31556952);
        emailCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
        VaadinService.getCurrentResponse().addCookie(emailCookie);
    }

    public static Optional<User> loadUser() {
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();

        boolean foundCookie = false;
        String name = "";
        String email = "";

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(TERRIBLE_PEOPLE_NAME)) {
                    try {
                        name = URLDecoder.decode(cookie.getValue(), "UTF-8");
                        foundCookie = true;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else if (cookie.getName().equals(TERRIBLE_PEOPLE_EMAIL)) {
                    try {
                        email = URLDecoder.decode(cookie.getValue(), "UTF-8");
                        foundCookie = true;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return foundCookie ? Optional.of(new User(name, email)) : Optional.empty();
    }
}
